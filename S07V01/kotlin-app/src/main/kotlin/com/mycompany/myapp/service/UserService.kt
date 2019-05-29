package com.mycompany.myapp.service

import com.mycompany.myapp.config.Constants
import com.mycompany.myapp.domain.Authority
import com.mycompany.myapp.domain.User
import com.mycompany.myapp.repository.AuthorityRepository
import com.mycompany.myapp.repository.UserRepository
import com.mycompany.myapp.security.AuthoritiesConstants
import com.mycompany.myapp.security.SecurityUtils
import com.mycompany.myapp.service.dto.UserDTO
import com.mycompany.myapp.service.util.RandomUtil
import com.mycompany.myapp.web.rest.errors.EmailAlreadyUsedException
import com.mycompany.myapp.web.rest.errors.InvalidPasswordException
import com.mycompany.myapp.web.rest.errors.LoginAlreadyUsedException

import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional

/**
 * Service class for managing users.
 */
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authorityRepository: AuthorityRepository,
    private val cacheManager: CacheManager
) {

    private val log = LoggerFactory.getLogger(UserService::class.java)

    fun activateRegistration(key: String): Optional<User> {
        log.debug("Activating user for activation key {}", key)
        return userRepository.findOneByActivationKey(key)
            .map { user ->
                // activate given user for the registration key.
                user.activated = true
                user.activationKey = null
                clearUserCaches(user)
                log.debug("Activated user: {}", user)
                user
            }
    }

    fun completePasswordReset(newPassword: String, key: String): Optional<User> {
        log.debug("Reset user password for reset key {}", key)
        return userRepository.findOneByResetKey(key)
            .filter { user -> user.resetDate?.isAfter(Instant.now().minusSeconds(86400)) ?: false }
            .map { user ->
                user.password = passwordEncoder.encode(newPassword)
                user.resetKey = null
                user.resetDate = null
                clearUserCaches(user)
                user
            }
    }

    fun requestPasswordReset(mail: String): Optional<User> {
        return userRepository.findOneByEmailIgnoreCase(mail)
            .filter(User::activated)
            .map { user ->
                user.resetKey = RandomUtil.generateResetKey()
                user.resetDate = Instant.now()
                clearUserCaches(user)
                user
            }
    }

    fun registerUser(userDTO: UserDTO, password: String): User {
        val login = userDTO.login ?: throw IllegalArgumentException("Empty login not allowed")
        val email = userDTO.email ?: throw IllegalArgumentException("Empty email not allowed")
        userRepository.findOneByLogin(login.toLowerCase()).ifPresent { existingUser ->
            val removed = removeNonActivatedUser(existingUser)
            if (!removed) {
                throw LoginAlreadyUsedException()
            }
        }
        userRepository.findOneByEmailIgnoreCase(email).ifPresent { existingUser ->
            val removed = removeNonActivatedUser(existingUser)
            if (!removed) {
                throw EmailAlreadyUsedException()
            }
        }
        val newUser = User()
        val encryptedPassword = passwordEncoder.encode(password)
        newUser.login = login.toLowerCase()
        // new user gets initially a generated password
        newUser.password = encryptedPassword
        newUser.firstName = userDTO.firstName
        newUser.lastName = userDTO.lastName
        newUser.email = email.toLowerCase()
        newUser.imageUrl = userDTO.imageUrl
        newUser.langKey = userDTO.langKey
        // new user is not active
        newUser.activated = false
        // new user gets registration key
        newUser.activationKey = RandomUtil.generateActivationKey()
        val authorities = mutableSetOf<Authority>()
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent { authorities.add(it) }
        newUser.authorities = authorities
        userRepository.save(newUser)
        clearUserCaches(newUser)
        log.debug("Created Information for User: {}", newUser)
        return newUser
    }

    private fun removeNonActivatedUser(existingUser: User): Boolean {
        if (existingUser.activated) {
            return false
        }
        userRepository.delete(existingUser)
        userRepository.flush()
        clearUserCaches(existingUser)
        return true
    }

    fun createUser(userDTO: UserDTO): User {
        val user = User()
        user.login = userDTO.login?.toLowerCase()
        user.firstName = userDTO.firstName
        user.lastName = userDTO.lastName
        user.email = userDTO.email?.toLowerCase()
        user.imageUrl = userDTO.imageUrl
        if (userDTO.langKey == null) {
            user.langKey = Constants.DEFAULT_LANGUAGE // default language
        } else {
            user.langKey = userDTO.langKey
        }
        val encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword())
        user.password = encryptedPassword
        user.resetKey = RandomUtil.generateResetKey()
        user.resetDate = Instant.now()
        user.activated = true
        userDTO.authorities?.apply {
            val authorities = this.asSequence()
                .map(authorityRepository::findById)
                .filter(Optional<Authority>::isPresent)
                .mapTo(mutableSetOf()) { it.get() }
            user.authorities = authorities
        }
        userRepository.save(user)
        clearUserCaches(user)
        log.debug("Created Information for User: {}", user)
        return user
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName last name of user.
     * @param email email id of user.
     * @param langKey language key.
     * @param imageUrl image URL of user.
     */
    fun updateUser(firstName: String?, lastName: String?, email: String?, langKey: String?, imageUrl: String?) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent { user ->
                user.firstName = firstName
                user.lastName = lastName
                user.email = email?.toLowerCase()
                user.langKey = langKey
                user.imageUrl = imageUrl
                clearUserCaches(user)
                log.debug("Changed Information for User: {}", user)
            }
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    fun updateUser(userDTO: UserDTO): Optional<UserDTO> {
        return Optional.of(userRepository.findById(userDTO.id!!))
            .filter(Optional<User>::isPresent)
            .map { it.get() }
            .map { user ->
                clearUserCaches(user)
                user.login = userDTO.login!!.toLowerCase()
                user.firstName = userDTO.firstName
                user.lastName = userDTO.lastName
                user.email = userDTO.email?.toLowerCase()
                user.imageUrl = userDTO.imageUrl
                user.activated = userDTO.activated
                user.langKey = userDTO.langKey
                val managedAuthorities = user.authorities
                managedAuthorities.clear()
                userDTO.authorities?.apply {
                    this.asSequence()
                        .map { authorityRepository.findById(it) }
                        .filter { it.isPresent }
                        .mapTo(managedAuthorities) { it.get() }
                }
                this.clearUserCaches(user)
                log.debug("Changed Information for User: {}", user)
                user
            }
            .map { UserDTO(it) }
    }

    fun deleteUser(login: String) {
        userRepository.findOneByLogin(login).ifPresent { user ->
            userRepository.delete(user)
            clearUserCaches(user)
            log.debug("Deleted User: {}", user)
        }
    }

    fun changePassword(currentClearTextPassword: String, newPassword: String) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent { user ->
                val currentEncryptedPassword = user.password
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw InvalidPasswordException()
                }
                val encryptedPassword = passwordEncoder.encode(newPassword)
                user.password = encryptedPassword
                clearUserCaches(user)
                log.debug("Changed password for User: {}", user)
            }
    }

    @Transactional(readOnly = true)
    fun getAllManagedUsers(pageable: Pageable): Page<UserDTO> {
        return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER).map { UserDTO(it) }
    }

    @Transactional(readOnly = true)
    fun getUserWithAuthoritiesByLogin(login: String): Optional<User> {
        return userRepository.findOneWithAuthoritiesByLogin(login)
    }

    @Transactional(readOnly = true)
    fun getUserWithAuthorities(id: Long): Optional<User> {
        return userRepository.findOneWithAuthoritiesById(id)
    }

    @Transactional(readOnly = true)
    fun getUserWithAuthorities(): Optional<User> {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin)
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     *
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    fun removeNotActivatedUsers() {
        userRepository
            .findAllByActivatedIsFalseAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
            .forEach { user ->
                log.debug("Deleting not activated user {}", user.login)
                userRepository.delete(user)
                clearUserCaches(user)
            }
    }

    /**
     * @return a list of all the authorities
     */
    fun getAuthorities(): MutableList<String> {
        return authorityRepository.findAll().asSequence().map { it.name }.filterNotNullTo(mutableListOf())
    }

    private fun clearUserCaches(user: User) {
        cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)?.evict(user.login!!)
        cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)?.evict(user.email!!)
    }
}

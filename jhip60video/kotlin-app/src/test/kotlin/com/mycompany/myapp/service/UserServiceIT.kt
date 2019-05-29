package com.mycompany.myapp.service

import com.mycompany.myapp.JhipsterApp
import com.mycompany.myapp.config.Constants
import com.mycompany.myapp.domain.User
import com.mycompany.myapp.repository.UserRepository
import com.mycompany.myapp.service.util.RandomUtil

import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.auditing.AuditingHandler
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAccessor
import java.time.LocalDateTime
import java.util.Optional

import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito.`when`
import kotlin.test.assertNotNull

/**
 * Integration tests for [UserService].
 */
@SpringBootTest(classes = [JhipsterApp::class])
class UserServiceIT {

    companion object {
        private const val DEFAULT_LOGIN = "johndoe"

        private const val DEFAULT_EMAIL = "johndoe@localhost"

        private const val DEFAULT_FIRSTNAME = "john"

        private const val DEFAULT_LASTNAME = "doe"

        private const val DEFAULT_IMAGEURL = "http://placehold.it/50x50"

        private const val DEFAULT_LANGKEY = "en"
    }

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var auditingHandler: AuditingHandler

    @Mock
    private lateinit var dateTimeProvider: DateTimeProvider

    private lateinit var user: User

    @BeforeEach
    fun init() {
        user = User()
        user.login = DEFAULT_LOGIN
        user.password = RandomStringUtils.random(60)
        user.activated = true
        user.email = DEFAULT_EMAIL
        user.firstName = DEFAULT_FIRSTNAME
        user.lastName = DEFAULT_LASTNAME
        user.imageUrl = DEFAULT_IMAGEURL
        user.langKey = DEFAULT_LANGKEY

        `when`<Optional<TemporalAccessor>>(dateTimeProvider.now).thenReturn(Optional.of(LocalDateTime.now()))
        auditingHandler.setDateTimeProvider(dateTimeProvider)
    }

    @Test
    @Transactional
    fun assertThatUserMustExistToResetPassword() {
        userRepository.saveAndFlush(user)
        var maybeUser = userService.requestPasswordReset("invalid.login@localhost")
        assertThat(maybeUser).isNotPresent

        maybeUser = userService.requestPasswordReset(user.email!!)
        assertThat(maybeUser).isPresent
        assertThat(maybeUser.orElse(null).email).isEqualTo(user.email)
        assertThat(maybeUser.orElse(null).resetDate).isNotNull()
        assertThat(maybeUser.orElse(null).resetKey).isNotNull()
    }

    @Test
    @Transactional
    fun assertThatOnlyActivatedUserCanRequestPasswordReset() {
        user.activated = false
        userRepository.saveAndFlush(user)

        val maybeUser = userService.requestPasswordReset(user.login!!)
        assertThat(maybeUser).isNotPresent
        userRepository.delete(user)
    }

    @Test
    @Transactional
    fun assertThatResetKeyMustNotBeOlderThan24Hours() {
        val daysAgo = Instant.now().minus(25, ChronoUnit.HOURS)
        val resetKey = RandomUtil.generateResetKey()
        user.activated = true
        user.resetDate = daysAgo
        user.resetKey = resetKey
        userRepository.saveAndFlush(user)

        val maybeUser = userService.completePasswordReset("johndoe2", user.resetKey!!)
        assertThat(maybeUser).isNotPresent
        userRepository.delete(user)
    }

    @Test
    @Transactional
    fun assertThatResetKeyMustBeValid() {
        val daysAgo = Instant.now().minus(25, ChronoUnit.HOURS)
        user.activated = true
        user.resetDate = daysAgo
        user.resetKey = "1234"
        userRepository.saveAndFlush(user)

        val maybeUser = userService.completePasswordReset("johndoe2", user.resetKey!!)
        assertThat(maybeUser).isNotPresent
        userRepository.delete(user)
    }

    @Test
    @Transactional
    fun assertThatUserCanResetPassword() {
        val oldPassword = user.password
        val daysAgo = Instant.now().minus(2, ChronoUnit.HOURS)
        val resetKey = RandomUtil.generateResetKey()
        user.activated = true
        user.resetDate = daysAgo
        user.resetKey = resetKey
        userRepository.saveAndFlush(user)

        val maybeUser = userService.completePasswordReset("johndoe2", user.resetKey!!)
        assertThat(maybeUser).isPresent
        assertThat(maybeUser.orElse(null).resetDate).isNull()
        assertThat(maybeUser.orElse(null).resetKey).isNull()
        assertThat(maybeUser.orElse(null).password).isNotEqualTo(oldPassword)

        userRepository.delete(user)
    }

    @Test
    @Transactional
    fun testFindNotActivatedUsersByCreationDateBefore() {
        val now = Instant.now()
        `when`<Optional<TemporalAccessor>>(dateTimeProvider.now).thenReturn(Optional.of(now.minus(4, ChronoUnit.DAYS)))
        user.activated = false
        val dbUser = userRepository.saveAndFlush(user)
        assertNotNull(dbUser)
        dbUser.createdDate = now.minus(4, ChronoUnit.DAYS)
        userRepository.saveAndFlush(user)
        var users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minus(3, ChronoUnit.DAYS))
        assertThat(users).isNotEmpty
        userService.removeNotActivatedUsers()
        users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minus(3, ChronoUnit.DAYS))
        assertThat(users).isEmpty()
    }

    @Test
    @Transactional
    fun assertThatAnonymousUserIsNotGet() {
        user.login = Constants.ANONYMOUS_USER
        if (!userRepository.findOneByLogin(Constants.ANONYMOUS_USER).isPresent) {
            userRepository.saveAndFlush(user)
        }
        val pageable = PageRequest.of(0, userRepository.count().toInt())
        val allManagedUsers = userService.getAllManagedUsers(pageable)
        assertNotNull(allManagedUsers)
        assertThat(allManagedUsers.content.stream()
            .noneMatch { user -> Constants.ANONYMOUS_USER == user.login })
            .isTrue()
    }

    @Test
    @Transactional
    fun testRemoveNotActivatedUsers() {
        // custom "now" for audit to use as creation date
        `when`<Optional<TemporalAccessor>>(dateTimeProvider.now).thenReturn(Optional.of(Instant.now().minus(30, ChronoUnit.DAYS)))

        user.activated = false
        userRepository.saveAndFlush(user)

        assertThat(userRepository.findOneByLogin(DEFAULT_LOGIN)).isPresent
        userService.removeNotActivatedUsers()
        assertThat(userRepository.findOneByLogin(DEFAULT_LOGIN)).isNotPresent
    }
}

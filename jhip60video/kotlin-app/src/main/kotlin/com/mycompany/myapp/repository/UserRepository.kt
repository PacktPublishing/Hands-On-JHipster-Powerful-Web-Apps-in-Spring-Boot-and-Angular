package com.mycompany.myapp.repository

import com.mycompany.myapp.domain.User

import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

import java.util.Optional
import java.time.Instant

/**
 * Spring Data JPA repository for the [User] entity.
 */
@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun findOneByActivationKey(activationKey: String): Optional<User>

    fun findAllByActivatedIsFalseAndCreatedDateBefore(dateTime: Instant): List<User>

    fun findOneByResetKey(resetKey: String): Optional<User>

    fun findOneByEmailIgnoreCase(email: String?): Optional<User>

    fun findOneByLogin(login: String): Optional<User>

    @EntityGraph(attributePaths = ["authorities"])
    fun findOneWithAuthoritiesById(id: Long): Optional<User>

    @EntityGraph(attributePaths = ["authorities"])
    @Cacheable(cacheNames = [USERS_BY_LOGIN_CACHE])
    fun findOneWithAuthoritiesByLogin(login: String): Optional<User>

    @EntityGraph(attributePaths = ["authorities"])
    @Cacheable(cacheNames = [USERS_BY_EMAIL_CACHE])
    fun findOneWithAuthoritiesByEmail(email: String): Optional<User>

    fun findAllByLoginNot(pageable: Pageable, login: String): Page<User>

    companion object {

        const val USERS_BY_LOGIN_CACHE: String = "usersByLogin"

        const val USERS_BY_EMAIL_CACHE: String = "usersByEmail"
    }
}

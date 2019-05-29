package com.mycompany.myapp.web.rest

import com.mycompany.myapp.JhipsterApp
import com.mycompany.myapp.domain.User
import com.mycompany.myapp.repository.UserRepository
import com.mycompany.myapp.security.jwt.TokenProvider
import com.mycompany.myapp.web.rest.errors.ExceptionTranslator
import com.mycompany.myapp.web.rest.vm.LoginVM
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.hamcrest.Matchers.nullValue
import org.hamcrest.Matchers.isEmptyString
import org.hamcrest.Matchers.not

/**
 * Integration tests for the [UserJWTController] REST controller.
 */
@SpringBootTest(classes = [JhipsterApp::class])
class UserJWTControllerIT {

    @Autowired
    private lateinit var tokenProvider: TokenProvider

    @Autowired
    private lateinit var authenticationManager: AuthenticationManagerBuilder

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var exceptionTranslator: ExceptionTranslator

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        val userJWTController = UserJWTController(tokenProvider, authenticationManager)
        this.mockMvc = MockMvcBuilders.standaloneSetup(userJWTController)
            .setControllerAdvice(exceptionTranslator)
            .build()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testAuthorize() {
        val user = User()
        user.login = "user-jwt-controller"
        user.email = "user-jwt-controller@example.com"
        user.activated = true
        user.password = passwordEncoder.encode("test")

        userRepository.saveAndFlush(user)

        val login = LoginVM()
        login.username = "user-jwt-controller"
        login.password = "test"
        mockMvc.perform(post("/api/authenticate")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(login)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("\$.id_token").isString)
            .andExpect(jsonPath("\$.id_token").isNotEmpty)
            .andExpect(header().string("Authorization", not(nullValue())))
            .andExpect(header().string("Authorization", not(isEmptyString())))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testAuthorizeWithRememberMe() {
        val user = User()
        user.login = "user-jwt-controller-remember-me"
        user.email = "user-jwt-controller-remember-me@example.com"
        user.activated = true
        user.password = passwordEncoder.encode("test")

        userRepository.saveAndFlush(user)

        val login = LoginVM()
        login.username = "user-jwt-controller-remember-me"
        login.password = "test"
        login.isRememberMe = true
        mockMvc.perform(post("/api/authenticate")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(login)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("\$.id_token").isString)
            .andExpect(jsonPath("\$.id_token").isNotEmpty)
            .andExpect(header().string("Authorization", not(nullValue())))
            .andExpect(header().string("Authorization", not(isEmptyString())))
    }

    @Test
    @Throws(Exception::class)
    fun testAuthorizeFails() {
        val login = LoginVM()
        login.username = "wrong-user"
        login.password = "wrong password"
        mockMvc.perform(post("/api/authenticate")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(login)))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("\$.id_token").doesNotExist())
            .andExpect(header().doesNotExist("Authorization"))
    }
}

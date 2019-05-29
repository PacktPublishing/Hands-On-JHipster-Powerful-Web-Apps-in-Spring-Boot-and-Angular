package com.mycompany.myapp.web.rest
import com.mycompany.myapp.JhipsterApp
import com.mycompany.myapp.config.Constants
import com.mycompany.myapp.domain.Authority
import com.mycompany.myapp.domain.User
import com.mycompany.myapp.repository.AuthorityRepository
import com.mycompany.myapp.repository.UserRepository
import com.mycompany.myapp.security.AuthoritiesConstants
import com.mycompany.myapp.service.MailService
import com.mycompany.myapp.service.UserService
import com.mycompany.myapp.service.dto.PasswordChangeDTO
import com.mycompany.myapp.service.dto.UserDTO
import com.mycompany.myapp.web.rest.errors.ExceptionTranslator
import com.mycompany.myapp.web.rest.vm.KeyAndPasswordVM
import com.mycompany.myapp.web.rest.vm.ManagedUserVM
import org.apache.commons.lang3.RandomStringUtils

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional

import java.time.Instant
import java.util.Optional

import org.assertj.core.api.Assertions.assertThat
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.nhaarman.mockitokotlin2.whenever
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.any

/**
 * Integrations tests for the [AccountResource] REST controller.
 */
@SpringBootTest(classes = [JhipsterApp::class])
class AccountResourceIT {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var authorityRepository: AuthorityRepository

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var httpMessageConverters: Array<HttpMessageConverter<*>>

    @Autowired
    private lateinit var exceptionTranslator: ExceptionTranslator

    @Mock
    private lateinit var mockUserService: UserService

    @Mock
    private lateinit var mockMailService: MailService

    private lateinit var restMvc: MockMvc

    private lateinit var restUserMockMvc: MockMvc

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        doNothing().whenever(mockMailService).sendActivationEmail(any())
        val accountResource = AccountResource(userRepository, userService, mockMailService)

        val accountUserMockResource =
            AccountResource(userRepository, mockUserService, mockMailService)
        this.restMvc = MockMvcBuilders.standaloneSetup(accountResource)
            .setMessageConverters(*httpMessageConverters)
            .setControllerAdvice(exceptionTranslator)
            .build()
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(accountUserMockResource)
            .setControllerAdvice(exceptionTranslator)
            .build()
    }

    @Test
    @Throws(Exception::class)
    fun testNonAuthenticatedUser() {
        restUserMockMvc.perform(
            get("/api/authenticate")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().string(""))
    }

    @Test
    @Throws(Exception::class)
    fun testAuthenticatedUser() {
        restUserMockMvc.perform(get("/api/authenticate")
            .with { request ->
                request.remoteUser = "test"
                request
            }
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().string("test"))
    }

    @Test
    @Throws(Exception::class)
    fun testGetExistingAccount() {
        val authorities = mutableSetOf<Authority>()
        val authority = Authority()
        authority.name = AuthoritiesConstants.ADMIN
        authorities.add(authority)

        val user = User()
        user.login = "test"
        user.firstName = "john"
        user.lastName = "doe"
        user.email = "john.doe@jhipster.com"
        user.imageUrl = "http://placehold.it/50x50"
        user.langKey = "en"
        user.authorities = authorities
        whenever(mockUserService.getUserWithAuthorities()).thenReturn(Optional.of(user))

        restUserMockMvc.perform(
            get("/api/account")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("\$.login").value("test"))
            .andExpect(jsonPath("\$.firstName").value("john"))
            .andExpect(jsonPath("\$.lastName").value("doe"))
            .andExpect(jsonPath("\$.email").value("john.doe@jhipster.com"))
            .andExpect(jsonPath("\$.imageUrl").value("http://placehold.it/50x50"))
            .andExpect(jsonPath("\$.langKey").value("en"))
            .andExpect(jsonPath("\$.authorities").value(AuthoritiesConstants.ADMIN))
    }

    @Test
    @Throws(Exception::class)
    fun testGetUnknownAccount() {
        whenever(mockUserService.getUserWithAuthorities()).thenReturn(Optional.empty())

        restUserMockMvc.perform(
            get("/api/account")
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
        )
            .andExpect(status().isInternalServerError)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterValid() {
        val validUser = ManagedUserVM()
        validUser.login = "test-register-valid"
        validUser.password = "password"
        validUser.firstName = "Alice"
        validUser.lastName = "Test"
        validUser.email = "test-register-valid@example.com"
        validUser.imageUrl = "http://placehold.it/50x50"
        validUser.langKey = Constants.DEFAULT_LANGUAGE
        validUser.authorities = setOf(AuthoritiesConstants.USER)
        assertThat(userRepository.findOneByLogin("test-register-valid").isPresent).isFalse()

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser))
        )
            .andExpect(status().isCreated)

        assertThat(userRepository.findOneByLogin("test-register-valid").isPresent).isTrue()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterInvalidLogin() {
        val invalidUser = ManagedUserVM()
        invalidUser.login = "funky-log!n" // <-- invalid
        invalidUser.password = "password"
        invalidUser.firstName = "Funky"
        invalidUser.lastName = "One"
        invalidUser.email = "funky@example.com"
        invalidUser.activated = true
        invalidUser.imageUrl = "http://placehold.it/50x50"
        invalidUser.langKey = Constants.DEFAULT_LANGUAGE
        invalidUser.authorities = setOf(AuthoritiesConstants.USER)

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser))
        )
            .andExpect(status().isBadRequest)

        val user = userRepository.findOneByEmailIgnoreCase("funky@example.com")
        assertThat(user.isPresent).isFalse()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterInvalidEmail() {
        val invalidUser = ManagedUserVM()
        invalidUser.login = "bob"
        invalidUser.password = "password"
        invalidUser.firstName = "Bob"
        invalidUser.lastName = "Green"
        invalidUser.email = "invalid" // <-- invalid
        invalidUser.activated = true
        invalidUser.imageUrl = "http://placehold.it/50x50"
        invalidUser.langKey = Constants.DEFAULT_LANGUAGE
        invalidUser.authorities = setOf(AuthoritiesConstants.USER)

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser))
        )
            .andExpect(status().isBadRequest)

        val user = userRepository.findOneByLogin("bob")
        assertThat(user.isPresent).isFalse()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterInvalidPassword() {
        val invalidUser = ManagedUserVM()
        invalidUser.login = "bob"
        invalidUser.password = "123" // password with only 3 digits
        invalidUser.firstName = "Bob"
        invalidUser.lastName = "Green"
        invalidUser.email = "bob@example.com"
        invalidUser.activated = true
        invalidUser.imageUrl = "http://placehold.it/50x50"
        invalidUser.langKey = Constants.DEFAULT_LANGUAGE
        invalidUser.authorities = setOf(AuthoritiesConstants.USER)

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser))
        )
            .andExpect(status().isBadRequest)

        val user = userRepository.findOneByLogin("bob")
        assertThat(user.isPresent).isFalse()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterNullPassword() {
        val invalidUser = ManagedUserVM()
        invalidUser.login = "bob"
        invalidUser.password = null // invalid null password
        invalidUser.firstName = "Bob"
        invalidUser.lastName = "Green"
        invalidUser.email = "bob@example.com"
        invalidUser.activated = true
        invalidUser.imageUrl = "http://placehold.it/50x50"
        invalidUser.langKey = Constants.DEFAULT_LANGUAGE
        invalidUser.authorities = setOf(AuthoritiesConstants.USER)

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser))
        )
            .andExpect(status().isBadRequest)

        val user = userRepository.findOneByLogin("bob")
        assertThat(user.isPresent).isFalse()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterDuplicateLogin() {
        // First registration
        val firstUser = ManagedUserVM()
        firstUser.login = "alice"
        firstUser.password = "password"
        firstUser.firstName = "Alice"
        firstUser.lastName = "Something"
        firstUser.email = "alice@example.com"
        firstUser.imageUrl = "http://placehold.it/50x50"
        firstUser.langKey = Constants.DEFAULT_LANGUAGE
        firstUser.authorities = setOf(AuthoritiesConstants.USER)

        // Duplicate login, different email
        val secondUser = ManagedUserVM()
        secondUser.login = firstUser.login
        secondUser.password = firstUser.password
        secondUser.firstName = firstUser.firstName
        secondUser.lastName = firstUser.lastName
        secondUser.email = "alice2@example.com"
        secondUser.imageUrl = firstUser.imageUrl
        secondUser.langKey = firstUser.langKey
        secondUser.createdBy = firstUser.createdBy
        secondUser.createdDate = firstUser.createdDate
        secondUser.lastModifiedBy = firstUser.lastModifiedBy
        secondUser.lastModifiedDate = firstUser.lastModifiedDate
        secondUser.authorities = firstUser.authorities?.toMutableSet()

        // First user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(firstUser))
        )
            .andExpect(status().isCreated)

        // Second (non activated) user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(secondUser)))
            .andExpect(status().isCreated)

        val testUser = userRepository.findOneByEmailIgnoreCase("alice2@example.com")
        assertThat(testUser.isPresent).isTrue()
        testUser.get().activated = true
        userRepository.save(testUser.get())

        // Second (already activated) user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(secondUser))
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterDuplicateEmail() {
        // First user
        val firstUser = ManagedUserVM()
        firstUser.login = "test-register-duplicate-email"
        firstUser.password = "password"
        firstUser.firstName = "Alice"
        firstUser.lastName = "Test"
        firstUser.email = "test-register-duplicate-email@example.com"
        firstUser.imageUrl = "http://placehold.it/50x50"
        firstUser.langKey = Constants.DEFAULT_LANGUAGE
        firstUser.authorities = setOf(AuthoritiesConstants.USER)

        // Register first user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(firstUser))
        )
            .andExpect(status().isCreated)

        val testUser1 = userRepository.findOneByLogin("test-register-duplicate-email")
        assertThat(testUser1.isPresent).isTrue()

        // Duplicate email, different login
        val secondUser = ManagedUserVM()
        secondUser.login = "test-register-duplicate-email-2"
        secondUser.password = firstUser.password
        secondUser.firstName = firstUser.firstName
        secondUser.lastName = firstUser.lastName
        secondUser.email = firstUser.email
        secondUser.imageUrl = firstUser.imageUrl
        secondUser.langKey = firstUser.langKey
        secondUser.authorities = firstUser.authorities?.toMutableSet()

        // Register second (non activated) user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(secondUser))
        )
            .andExpect(status().isCreated)

        val testUser2 = userRepository.findOneByLogin("test-register-duplicate-email")
        assertThat(testUser2.isPresent).isFalse()

        val testUser3 = userRepository.findOneByLogin("test-register-duplicate-email-2")
        assertThat(testUser3.isPresent).isTrue()

        // Duplicate email - with uppercase email address
        val userWithUpperCaseEmail = ManagedUserVM()
        userWithUpperCaseEmail.id = firstUser.id
        userWithUpperCaseEmail.login = "test-register-duplicate-email-3"
        userWithUpperCaseEmail.password = firstUser.password
        userWithUpperCaseEmail.firstName = firstUser.firstName
        userWithUpperCaseEmail.lastName = firstUser.lastName
        userWithUpperCaseEmail.email = "TEST-register-duplicate-email@example.com"
        userWithUpperCaseEmail.imageUrl = firstUser.imageUrl
        userWithUpperCaseEmail.langKey = firstUser.langKey
        userWithUpperCaseEmail.authorities = firstUser.authorities?.toMutableSet()

        // Register third (not activated) user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userWithUpperCaseEmail))
        )
            .andExpect(status().isCreated)

        val testUser4 = userRepository.findOneByLogin("test-register-duplicate-email-3")
        assertThat(testUser4.isPresent).isTrue()
        assertThat(testUser4.get().email).isEqualTo("test-register-duplicate-email@example.com")

        testUser4.get().activated = true
        userService.updateUser((UserDTO(testUser4.get())))

        // Register 4th (already activated) user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(secondUser))
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRegisterAdminIsIgnored() {
        val validUser = ManagedUserVM()
        validUser.login = "badguy"
        validUser.password = "password"
        validUser.firstName = "Bad"
        validUser.lastName = "Guy"
        validUser.email = "badguy@example.com"
        validUser.activated = true
        validUser.imageUrl = "http://placehold.it/50x50"
        validUser.langKey = Constants.DEFAULT_LANGUAGE
        validUser.authorities = setOf(AuthoritiesConstants.ADMIN)

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser))
        )
            .andExpect(status().isCreated)

        val userDup = userRepository.findOneByLogin("badguy")
        assertThat(userDup.isPresent).isTrue()
        assertThat(userDup.get().authorities).hasSize(1)
            .containsExactly(authorityRepository.findById(AuthoritiesConstants.USER).get())
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testActivateAccount() {
        val activationKey = "some activation key"
        var user = User()
        user.login = "activate-account"
        user.email = "activate-account@example.com"
        user.password = RandomStringUtils.random(60)
        user.activated = false
        user.activationKey = activationKey

        userRepository.saveAndFlush(user)

        restMvc.perform(get("/api/activate?key={activationKey}", activationKey))
            .andExpect(status().isOk)

        user = userRepository.findOneByLogin(user.login!!).orElse(null)
        assertThat(user.activated).isTrue()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testActivateAccountWithWrongKey() {
        restMvc.perform(get("/api/activate?key=wrongActivationKey"))
            .andExpect(status().isInternalServerError)
    }

    @Test
    @Transactional
    @WithMockUser("save-account")
    @Throws(Exception::class)
    fun testSaveAccount() {
        val user = User()
        user.login = "save-account"
        user.email = "save-account@example.com"
        user.password = RandomStringUtils.random(60)
        user.activated = true

        userRepository.saveAndFlush(user)

        val userDTO = UserDTO()
        userDTO.login = "not-used"
        userDTO.firstName = "firstname"
        userDTO.lastName = "lastname"
        userDTO.email = "save-account@example.com"
        userDTO.activated = false
        userDTO.imageUrl = "http://placehold.it/50x50"
        userDTO.langKey = Constants.DEFAULT_LANGUAGE
        userDTO.authorities = setOf(AuthoritiesConstants.ADMIN)

        restMvc.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO))
        )
            .andExpect(status().isOk)

        val updatedUser = userRepository.findOneByLogin(user.login!!).orElse(null)
        assertThat(updatedUser.firstName).isEqualTo(userDTO.firstName)
        assertThat(updatedUser.lastName).isEqualTo(userDTO.lastName)
        assertThat(updatedUser.email).isEqualTo(userDTO.email)
        assertThat(updatedUser.langKey).isEqualTo(userDTO.langKey)
        assertThat(updatedUser.password).isEqualTo(user.password)
        assertThat(updatedUser.imageUrl).isEqualTo(userDTO.imageUrl)
        assertThat(updatedUser.activated).isEqualTo(true)
        assertThat(updatedUser.authorities).isEmpty()
    }

    @Test
    @Transactional
    @WithMockUser("save-invalid-email")
    @Throws(Exception::class)
    fun testSaveInvalidEmail() {
        val user = User()
        user.login = "save-invalid-email"
        user.email = "save-invalid-email@example.com"
        user.password = RandomStringUtils.random(60)
        user.activated = true

        userRepository.saveAndFlush(user)

        val userDTO = UserDTO()
        userDTO.login = "not-used"
        userDTO.firstName = "firstname"
        userDTO.lastName = "lastname"
        userDTO.email = "invalid email"
        userDTO.activated = false
        userDTO.imageUrl = "http://placehold.it/50x50"
        userDTO.langKey = Constants.DEFAULT_LANGUAGE
        userDTO.authorities = setOf(AuthoritiesConstants.ADMIN)

        restMvc.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO))
        )
            .andExpect(status().isBadRequest)

        assertThat(userRepository.findOneByEmailIgnoreCase("invalid email")).isNotPresent
    }

    @Test
    @Transactional
    @WithMockUser("save-existing-email")
    @Throws(Exception::class)
    fun testSaveExistingEmail() {
        val user = User()
        user.login = "save-existing-email"
        user.email = "save-existing-email@example.com"
        user.password = RandomStringUtils.random(60)
        user.activated = true

        userRepository.saveAndFlush(user)

        val anotherUser = User()
        anotherUser.login = "save-existing-email2"
        anotherUser.email = "save-existing-email2@example.com"
        anotherUser.password = RandomStringUtils.random(60)
        anotherUser.activated = true

        userRepository.saveAndFlush(anotherUser)

        val userDTO = UserDTO()
        userDTO.login = "not-used"
        userDTO.firstName = "firstname"
        userDTO.lastName = "lastname"
        userDTO.email = "save-existing-email2@example.com"
        userDTO.activated = false
        userDTO.imageUrl = "http://placehold.it/50x50"
        userDTO.langKey = Constants.DEFAULT_LANGUAGE
        userDTO.authorities = setOf(AuthoritiesConstants.ADMIN)

        restMvc.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO))
        )
            .andExpect(status().isBadRequest)

        val updatedUser = userRepository.findOneByLogin("save-existing-email").orElse(null)
        assertThat(updatedUser.email).isEqualTo("save-existing-email@example.com")
    }

    @Test
    @Transactional
    @WithMockUser("save-existing-email-and-login")
    @Throws(Exception::class)
    fun testSaveExistingEmailAndLogin() {
        val user = User()
        user.login = "save-existing-email-and-login"
        user.email = "save-existing-email-and-login@example.com"
        user.password = RandomStringUtils.random(60)
        user.activated = true

        userRepository.saveAndFlush(user)

        val userDTO = UserDTO()
        userDTO.login = "not-used"
        userDTO.firstName = "firstname"
        userDTO.lastName = "lastname"
        userDTO.email = "save-existing-email-and-login@example.com"
        userDTO.activated = false
        userDTO.imageUrl = "http://placehold.it/50x50"
        userDTO.langKey = Constants.DEFAULT_LANGUAGE
        userDTO.authorities = setOf(AuthoritiesConstants.ADMIN)

        restMvc.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO))
        )
            .andExpect(status().isOk)

        val updatedUser = userRepository.findOneByLogin("save-existing-email-and-login").orElse(null)
        assertThat(updatedUser.email).isEqualTo("save-existing-email-and-login@example.com")
    }

    @Test
    @Transactional
    @WithMockUser("change-password-wrong-existing-password")
    @Throws(Exception::class)
    fun testChangePasswordWrongExistingPassword() {
        val user = User()
        val currentPassword = RandomStringUtils.random(60)
        user.password = passwordEncoder.encode(currentPassword)
        user.login = "change-password-wrong-existing-password"
        user.email = "change-password-wrong-existing-password@example.com"

        userRepository.saveAndFlush(user)

        restMvc.perform(
            post("/api/account/change-password")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(PasswordChangeDTO("1$currentPassword", "new password")))
        )
            .andExpect(status().isBadRequest)

        val updatedUser = userRepository.findOneByLogin("change-password-wrong-existing-password").orElse(null)
        assertThat(passwordEncoder.matches("new password", updatedUser.password)).isFalse()
        assertThat(passwordEncoder.matches(currentPassword, updatedUser.password)).isTrue()
    }

    @Test
    @Transactional
    @WithMockUser("change-password")
    @Throws(Exception::class)
    fun testChangePassword() {
        val user = User()
        val currentPassword = RandomStringUtils.random(60)
        user.password = passwordEncoder.encode(currentPassword)
        user.login = "change-password"
        user.email = "change-password@example.com"

        userRepository.saveAndFlush(user)

        restMvc.perform(
            post("/api/account/change-password")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(PasswordChangeDTO(currentPassword, "new password")))
        )
            .andExpect(status().isOk)

        val updatedUser = userRepository.findOneByLogin("change-password").orElse(null)
        assertThat(passwordEncoder.matches("new password", updatedUser.password)).isTrue()
    }

    @Test
    @Transactional
    @WithMockUser("change-password-too-small")
    @Throws(Exception::class)
    fun testChangePasswordTooSmall() {
        val user = User()
        val currentPassword = RandomStringUtils.random(60)
        user.password = passwordEncoder.encode(currentPassword)
        user.login = "change-password-too-small"
        user.email = "change-password-too-small@example.com"

        userRepository.saveAndFlush(user)

        val newPassword = RandomStringUtils.random(ManagedUserVM.PASSWORD_MIN_LENGTH - 1)

        restMvc.perform(
            post("/api/account/change-password")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(PasswordChangeDTO(currentPassword, newPassword)))
        )
            .andExpect(status().isBadRequest)

        val updatedUser = userRepository.findOneByLogin("change-password-too-small").orElse(null)
        assertThat(updatedUser.password).isEqualTo(user.password)
    }

    @Test
    @Transactional
    @WithMockUser("change-password-too-long")
    @Throws(Exception::class)
    fun testChangePasswordTooLong() {
        val user = User()
        val currentPassword = RandomStringUtils.random(60)
        user.password = passwordEncoder.encode(currentPassword)
        user.login = "change-password-too-long"
        user.email = "change-password-too-long@example.com"

        userRepository.saveAndFlush(user)

        val newPassword = RandomStringUtils.random(ManagedUserVM.PASSWORD_MAX_LENGTH + 1)

        restMvc.perform(
            post("/api/account/change-password")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(PasswordChangeDTO(currentPassword, newPassword)))
        )
            .andExpect(status().isBadRequest)

        val updatedUser = userRepository.findOneByLogin("change-password-too-long").orElse(null)
        assertThat(updatedUser.password).isEqualTo(user.password)
    }

    @Test
    @Transactional
    @WithMockUser("change-password-empty")
    @Throws(Exception::class)
    fun testChangePasswordEmpty() {
        val user = User()
        val currentPassword = RandomStringUtils.random(60)
        user.password = passwordEncoder.encode(currentPassword)
        user.login = "change-password-empty"
        user.email = "change-password-empty@example.com"

        userRepository.saveAndFlush(user)

        restMvc.perform(
            post("/api/account/change-password")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(PasswordChangeDTO(currentPassword, "")))
        )
            .andExpect(status().isBadRequest)

        val updatedUser = userRepository.findOneByLogin("change-password-empty").orElse(null)
        assertThat(updatedUser.password).isEqualTo(user.password)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRequestPasswordReset() {
        val user = User()
        user.password = RandomStringUtils.random(60)
        user.activated = true
        user.login = "password-reset"

        user.email = "password-reset@example.com"
        userRepository.saveAndFlush(user)

        restMvc.perform(
            post("/api/account/reset-password/init")
                .content("password-reset@example.com")
        )
            .andExpect(status().isOk)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testRequestPasswordResetUpperCaseEmail() {
        val user = User()
        user.password = RandomStringUtils.random(60)
        user.activated = true
        user.login = "password-reset"
        user.email = "password-reset@example.com"

        userRepository.saveAndFlush(user)

        restMvc.perform(
            post("/api/account/reset-password/init")
                .content("password-reset@EXAMPLE.COM")
        )
            .andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun testRequestPasswordResetWrongEmail() {
        restMvc.perform(
            post("/api/account/reset-password/init")
                .content("password-reset-wrong-email@example.com"))
            .andExpect(status().isBadRequest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testFinishPasswordReset() {
        val user = User()
        user.password = RandomStringUtils.random(60)
        user.login = "finish-password-reset"
        user.email = "finish-password-reset@example.com"
        user.resetDate = Instant.now().plusSeconds(60)
        user.resetKey = "reset key"

        userRepository.saveAndFlush(user)

        val keyAndPassword = KeyAndPasswordVM()
        keyAndPassword.key = user.resetKey
        keyAndPassword.newPassword = "new password"

        restMvc.perform(
            post("/api/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword))
        )
            .andExpect(status().isOk)

        val updatedUser = userRepository.findOneByLogin(user.login!!).orElse(null)
        assertThat(passwordEncoder.matches(keyAndPassword.newPassword, updatedUser.password)).isTrue()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testFinishPasswordResetTooSmall() {
        val user = User()
        user.password = RandomStringUtils.random(60)
        user.login = "finish-password-reset-too-small"
        user.email = "finish-password-reset-too-small@example.com"
        user.resetDate = Instant.now().plusSeconds(60)
        user.resetKey = "reset key too small"

        userRepository.saveAndFlush(user)

        val keyAndPassword = KeyAndPasswordVM()
        keyAndPassword.key = user.resetKey
        keyAndPassword.newPassword = "foo"

        restMvc.perform(
            post("/api/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword))
        )
            .andExpect(status().isBadRequest)

        val updatedUser = userRepository.findOneByLogin(user.login!!).orElse(null)
        assertThat(passwordEncoder.matches(keyAndPassword.newPassword, updatedUser.password)).isFalse()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testFinishPasswordResetWrongKey() {
        val keyAndPassword = KeyAndPasswordVM()
        keyAndPassword.key = "wrong reset key"
        keyAndPassword.newPassword = "new password"

        restMvc.perform(
            post("/api/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword))
        )
            .andExpect(status().isInternalServerError)
    }
}

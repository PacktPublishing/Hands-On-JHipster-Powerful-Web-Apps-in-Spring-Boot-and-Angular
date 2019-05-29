package com.mycompany.myapp.service.mapper

import com.mycompany.myapp.JhipsterApp
import com.mycompany.myapp.domain.User
import com.mycompany.myapp.service.dto.UserDTO
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Integration tests for [UserMapper].
 */
@SpringBootTest(classes = [JhipsterApp::class])
class UserMapperIT {

    @Autowired
    private lateinit var userMapper: UserMapper

    private lateinit var user: User
    private lateinit var userDto: UserDTO

    @BeforeEach
    fun init() {
        user = User()
        user.login = DEFAULT_LOGIN
        user.password = RandomStringUtils.random(60)
        user.activated = true
        user.email = "johndoe@localhost"
        user.firstName = "john"
        user.lastName = "doe"
        user.imageUrl = "image_url"
        user.langKey = "en"

        userDto = UserDTO(user)
    }

    @Test
    fun usersToUserDTOsShouldMapOnlyNonNullUsers() {
        val users = mutableListOf<User?>()
        users.add(user)
        users.add(null)

        val userDTOS = userMapper.usersToUserDTOs(users)

        assertThat(userDTOS).isNotEmpty
        assertThat(userDTOS).size().isEqualTo(1)
    }

    @Test
    fun userDTOsToUsersShouldMapOnlyNonNullUsers() {
        val usersDto = mutableListOf<UserDTO?>()
        usersDto.add(userDto)
        usersDto.add(null)

        val users = userMapper.userDTOsToUsers(usersDto)

        assertThat(users).isNotEmpty
        assertThat(users).size().isEqualTo(1)
    }

    @Test
    fun userDTOsToUsersWithAuthoritiesStringShouldMapToUsersWithAuthoritiesDomain() {
        val authoritiesAsString = mutableSetOf<String>()
        authoritiesAsString.add("ADMIN")
        userDto.authorities = authoritiesAsString

        val usersDto = mutableListOf<UserDTO>()
        usersDto.add(userDto)

        val users = userMapper.userDTOsToUsers(usersDto)

        assertThat(users).isNotEmpty
        assertThat(users).size().isEqualTo(1)
        assertThat(users[0].authorities).isNotNull
        assertThat(users[0].authorities).isNotEmpty
        assertThat(users[0].authorities.first().name).isEqualTo("ADMIN")
    }

    @Test
    fun userDTOsToUsersMapWithNullAuthoritiesStringShouldReturnUserWithEmptyAuthorities() {
        userDto.authorities = null

        val usersDto = mutableListOf<UserDTO>()
        usersDto.add(userDto)

        val users = userMapper.userDTOsToUsers(usersDto)

        assertThat(users).isNotEmpty
        assertThat(users).size().isEqualTo(1)
        assertThat(users[0].authorities).isNotNull
        assertThat(users[0].authorities).isEmpty()
    }

    @Test
    fun userDTOToUserMapWithAuthoritiesStringShouldReturnUserWithAuthorities() {
        val authoritiesAsString = mutableSetOf<String>()
        authoritiesAsString.add("ADMIN")
        userDto.authorities = authoritiesAsString

        val user = userMapper.userDTOToUser(userDto)

        assertNotNull(user)
        assertThat(user.authorities).isNotNull
        assertThat(user.authorities).isNotEmpty
        assertThat(user.authorities.first().name).isEqualTo("ADMIN")
    }

    @Test
    fun userDTOToUserMapWithNullAuthoritiesStringShouldReturnUserWithEmptyAuthorities() {
        userDto.authorities = null

        val user = userMapper.userDTOToUser(userDto)

        assertNotNull(user)
        assertThat(user.authorities).isNotNull
        assertThat(user.authorities).isEmpty()
    }

    @Test
    fun userDTOToUserMapWithNullUserShouldReturnNull() {
        assertNull(userMapper.userDTOToUser(null))
    }

    @Test
    fun testUserFromId() {
        assertThat(userMapper.userFromId(DEFAULT_ID)?.id).isEqualTo(DEFAULT_ID)
        assertNull(userMapper.userFromId(null))
    }

    companion object {
        private const val DEFAULT_LOGIN = "johndoe"

        private const val DEFAULT_ID = 1L
    }
}

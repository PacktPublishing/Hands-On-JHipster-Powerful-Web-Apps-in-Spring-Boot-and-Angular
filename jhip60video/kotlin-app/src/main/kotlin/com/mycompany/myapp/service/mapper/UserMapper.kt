package com.mycompany.myapp.service.mapper

import com.mycompany.myapp.domain.Authority
import com.mycompany.myapp.domain.User
import com.mycompany.myapp.service.dto.UserDTO

import org.springframework.stereotype.Service

/**
 * Mapper for the entity [User] and its DTO called [UserDTO].
 *
 * Normal mappers are generated using MapStruct, this one is hand-coded as MapStruct
 * support is still in beta, and requires a manual step with an IDE.
 */
@Service
class UserMapper {

    fun usersToUserDTOs(users: List<User?>): MutableList<UserDTO> {
        return users.asSequence()
            .filterNotNull()
            .mapTo(mutableListOf()) { this.userToUserDTO(it) }
    }

    fun userToUserDTO(user: User): UserDTO {
        return UserDTO(user)
    }

    fun userDTOsToUsers(userDTOs: List<UserDTO?>): MutableList<User> {
        return userDTOs.asSequence()
            .map { userDTOToUser(it) }
            .filterNotNullTo(mutableListOf())
    }

    fun userDTOToUser(userDTO: UserDTO?): User? {
        return when (userDTO) {
            null -> null
            else -> {
                val user = User()
                user.id = userDTO.id
                user.login = userDTO.login
                user.firstName = userDTO.firstName
                user.lastName = userDTO.lastName
                user.email = userDTO.email
                user.imageUrl = userDTO.imageUrl
                user.activated = userDTO.activated
                user.langKey = userDTO.langKey
                user.authorities = authoritiesFromStrings(userDTO.authorities)
                user
            }
        }
    }

    private fun authoritiesFromStrings(authoritiesAsString: Set<String>?): MutableSet<Authority> {
        return authoritiesAsString?.mapTo(mutableSetOf()) {
            val auth = Authority()
            auth.name = it
            auth
        } ?: mutableSetOf()
    }

    fun userFromId(id: Long?): User? {
        if (id == null) {
            return null
        }
        val user = User()
        user.id = id
        return user
    }
}

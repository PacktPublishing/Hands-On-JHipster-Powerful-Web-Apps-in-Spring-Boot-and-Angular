package com.mycompany.myapp.repository

import com.mycompany.myapp.domain.Authority

import org.springframework.data.jpa.repository.JpaRepository

/**
 * Spring Data JPA repository for the [Authority] entity.
 */

interface AuthorityRepository : JpaRepository<Authority, String>

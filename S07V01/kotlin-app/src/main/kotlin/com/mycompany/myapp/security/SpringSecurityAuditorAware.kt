package com.mycompany.myapp.security

import com.mycompany.myapp.config.Constants

import java.util.Optional

import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component

/**
 * Implementation of [AuditorAware] based on Spring Security.
 */
@Component
class SpringSecurityAuditorAware : AuditorAware<String> {

    override fun getCurrentAuditor(): Optional<String> {
        return Optional.of(SecurityUtils.getCurrentUserLogin().orElse(Constants.SYSTEM_ACCOUNT))
    }
}

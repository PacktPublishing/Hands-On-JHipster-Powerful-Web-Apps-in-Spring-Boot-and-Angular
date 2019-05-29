package com.mycompany.myapp.web.rest.errors

import org.zalando.problem.Exceptional

class LoginAlreadyUsedException : BadRequestAlertException(ErrorConstants.LOGIN_ALREADY_USED_TYPE, "Login name already used!", "userManagement", "userexists") {

    override fun getCause(): Exceptional? {
        return super.cause
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}

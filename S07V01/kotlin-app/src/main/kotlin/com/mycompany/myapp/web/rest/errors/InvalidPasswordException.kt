package com.mycompany.myapp.web.rest.errors

import org.zalando.problem.AbstractThrowableProblem
import org.zalando.problem.Exceptional
import org.zalando.problem.Status

class InvalidPasswordException : AbstractThrowableProblem(ErrorConstants.INVALID_PASSWORD_TYPE, "Incorrect password", Status.BAD_REQUEST) {

    override fun getCause(): Exceptional? {
        return super.cause
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}

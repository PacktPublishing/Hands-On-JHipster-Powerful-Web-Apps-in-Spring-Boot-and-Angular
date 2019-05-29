package com.mycompany.myapp.web.rest.errors

import org.zalando.problem.AbstractThrowableProblem
import org.zalando.problem.Exceptional
import org.zalando.problem.Status

import java.net.URI

open class BadRequestAlertException(type: URI, defaultMessage: String, val entityName: String, val errorKey: String) :
    AbstractThrowableProblem(type, defaultMessage, Status.BAD_REQUEST, null, null, null, getAlertParameters(entityName, errorKey)) {

    constructor(defaultMessage: String, entityName: String, errorKey: String) : this(ErrorConstants.DEFAULT_TYPE, defaultMessage, entityName, errorKey)

    override fun getCause(): Exceptional? {
        return super.cause
    }

    companion object {

        private const val serialVersionUID = 1L

        private fun getAlertParameters(entityName: String, errorKey: String): Map<String, Any> {
            val parameters = mutableMapOf<String, Any>()
            parameters["message"] = "error.$errorKey"
            parameters["params"] = entityName
            return parameters
        }
    }
}

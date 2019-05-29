package com.mycompany.myapp.service.util

import org.apache.commons.lang3.RandomStringUtils

/**
 * Utility class for generating random Strings.
 */
object RandomUtil {

    private const val DEF_COUNT = 20

    /**
     * Generate a password.
     *
     * @return the generated password.
     */
    @JvmStatic
    fun generatePassword(): String {
        return RandomStringUtils.randomAlphanumeric(DEF_COUNT)
    }

    /**
     * Generate an activation key.
     *
     * @return the generated activation key.
     */
    @JvmStatic
    fun generateActivationKey(): String {
        return RandomStringUtils.randomNumeric(DEF_COUNT)
    }

    /**
     * Generate a reset key.
     *
     * @return the generated reset key.
     */
    @JvmStatic
    fun generateResetKey(): String {
        return RandomStringUtils.randomNumeric(DEF_COUNT)
    }
}

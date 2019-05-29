package com.mycompany.myapp.web.rest

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.hamcrest.Description
import org.hamcrest.TypeSafeDiagnosingMatcher
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar
import org.springframework.format.support.DefaultFormattingConversionService
import org.springframework.format.support.FormattingConversionService
import org.springframework.http.MediaType
import javax.persistence.EntityManager

import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

import org.assertj.core.api.Assertions.assertThat

/**
 * Utility class for testing REST controllers.
 */
object TestUtil {

    private val mapper = createObjectMapper()

    /** MediaType for JSON UTF8  */
    @JvmField
    val APPLICATION_JSON_UTF8: MediaType = MediaType.APPLICATION_JSON_UTF8

    private fun createObjectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        mapper.registerModule(JavaTimeModule())
        return mapper
    }

    /**
     * Convert an object to JSON byte array.
     *
     * @param object the object to convert.
     * @return the JSON byte array.
     * @throws IOException
     */
    @JvmStatic
    @Throws(IOException::class)
    fun convertObjectToJsonBytes(`object`: Any): ByteArray {
        return mapper.writeValueAsBytes(`object`)
    }

    /**
     * Create a byte array with a specific size filled with specified data.
     *
     * @param size the size of the byte array.
     * @param data the data to put in the byte array.
     * @return the JSON byte array.
     */
    @JvmStatic
    fun createByteArray(size: Int, data: String): ByteArray {
        val byteArray = ByteArray(size)
        for (i in 0 until size) {
            byteArray[i] = java.lang.Byte.parseByte(data, 2)
        }
        return byteArray
    }

    /**
     * A matcher that tests that the examined string represents the same instant as the reference datetime.
     */
    class ZonedDateTimeMatcher(private val date: ZonedDateTime) : TypeSafeDiagnosingMatcher<String>() {

        override fun matchesSafely(item: String, mismatchDescription: Description): Boolean {
            try {
                if (!date.isEqual(ZonedDateTime.parse(item))) {
                    mismatchDescription.appendText("was ").appendValue(item)
                    return false
                }
                return true
            } catch (e: DateTimeParseException) {
                mismatchDescription.appendText("was ").appendValue(item)
                    .appendText(", which could not be parsed as a ZonedDateTime")
                return false
            }
        }

        override fun describeTo(description: Description) {
            description.appendText("a String representing the same Instant as ").appendValue(date)
        }
    }

    /**
     * Creates a matcher that matches when the examined string represents the same instant as the reference datetime.
     * @param date the reference datetime against which the examined string is checked.
     */
    @JvmStatic
    fun sameInstant(date: ZonedDateTime): ZonedDateTimeMatcher {
        return ZonedDateTimeMatcher(date)
    }

    /**
     * Verifies the equals/hashcode contract on the domain object.
     */
    @JvmStatic
    @Throws(Exception::class)
    fun <T> equalsVerifier(clazz: Class<T>) {
        val domainObject1 = clazz.getConstructor().newInstance()
        assertThat(domainObject1.toString()).isNotNull()
        assertThat(domainObject1).isEqualTo(domainObject1)
        assertThat(domainObject1.hashCode()).isEqualTo(domainObject1.hashCode())
        // Test with an instance of another class
        val testOtherObject = Any()
        assertThat(domainObject1).isNotEqualTo(testOtherObject)
        assertThat(domainObject1).isNotEqualTo(null)
        // Test with an instance of the same class
        val domainObject2 = clazz.getConstructor().newInstance()
        assertThat(domainObject1).isNotEqualTo(domainObject2)
        // HashCodes are equals because the objects are not persisted yet
        assertThat(domainObject1.hashCode()).isEqualTo(domainObject2.hashCode())
    }

    /**
     * Create a [FormattingConversionService] which use ISO date format, instead of the localized one.
     * @return the created [FormattingConversionService].
     */
    @JvmStatic
    fun createFormattingConversionService(): FormattingConversionService {
        val dfcs = DefaultFormattingConversionService()
        val registrar = DateTimeFormatterRegistrar()
        registrar.setUseIsoFormat(true)
        registrar.registerFormatters(dfcs)
        return dfcs
    }

    /**
     * Makes a an executes a query to the EntityManager finding all stored objects.
     * @param <T> The type of objects to be searched
     * @param em The instance of the EntityManager
     * @param clss The class type to be searched
     * @return A list of all found objects
     */
    @JvmStatic
    fun <T> findAll(em: EntityManager, clazz: Class<T>): List<T> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(clazz)
        val rootEntry = cq.from(clazz)
        val all = cq.select(rootEntry)
        val allQuery = em.createQuery(all)
        return allQuery.resultList
    }
}

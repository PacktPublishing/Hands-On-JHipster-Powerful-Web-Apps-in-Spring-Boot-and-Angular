package com.mycompany.myapp.config

import java.net.InetSocketAddress

import io.github.jhipster.config.JHipsterProperties

import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.boolex.OnMarkerEvaluator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.LoggerContextListener
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.filter.EvaluatorFilter
import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.core.spi.FilterReply
import net.logstash.logback.appender.LogstashTcpSocketAppender
import net.logstash.logback.composite.ContextJsonProvider
import net.logstash.logback.composite.GlobalCustomFieldsJsonProvider
import net.logstash.logback.composite.loggingevent.ArgumentsJsonProvider
import net.logstash.logback.composite.loggingevent.LogLevelJsonProvider
import net.logstash.logback.composite.loggingevent.LoggerNameJsonProvider
import net.logstash.logback.composite.loggingevent.LoggingEventFormattedTimestampJsonProvider
import net.logstash.logback.composite.loggingevent.LoggingEventJsonProviders
import net.logstash.logback.composite.loggingevent.LoggingEventPatternJsonProvider
import net.logstash.logback.composite.loggingevent.MdcJsonProvider
import net.logstash.logback.composite.loggingevent.MessageJsonProvider
import net.logstash.logback.composite.loggingevent.StackTraceJsonProvider
import net.logstash.logback.composite.loggingevent.ThreadNameJsonProvider
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder
import net.logstash.logback.encoder.LogstashEncoder
import net.logstash.logback.stacktrace.ShortenedThrowableConverter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class LoggingConfiguration(
    @param:Value("\${spring.application.name}") private val appName: String,
    @param:Value("\${server.port}") private val serverPort: String,
    private val jHipsterProperties: JHipsterProperties
) {

    private val log = LoggerFactory.getLogger(LoggingConfiguration::class.java)

    private val context = LoggerFactory.getILoggerFactory() as LoggerContext

    init {
        if (jHipsterProperties.logging.isUseJsonFormat) {
            addJsonConsoleAppender(context)
        }
        if (jHipsterProperties.logging.logstash.isEnabled) {
            addLogstashTcpSocketAppender(context)
        }
        if (jHipsterProperties.logging.isUseJsonFormat || jHipsterProperties.logging.logstash.isEnabled) {
            addContextListener(context)
        }
        if (jHipsterProperties.metrics.logs.isEnabled) {
            setMetricsMarkerLogbackFilter(context)
        }
    }

    private fun addJsonConsoleAppender(context: LoggerContext) {
        log.info("Initializing Console logging")

        // More documentation is available at: https://github.com/logstash/logstash-logback-encoder
        val consoleAppender = ConsoleAppender<ILoggingEvent>()
        consoleAppender.context = context
        consoleAppender.encoder = compositeJsonEncoder(context)
        consoleAppender.name = CONSOLE_APPENDER_NAME
        consoleAppender.start()

        context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME).detachAppender(CONSOLE_APPENDER_NAME)
        context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME).addAppender(consoleAppender)
    }

    private fun addLogstashTcpSocketAppender(context: LoggerContext) {
        log.info("Initializing Logstash logging")

        // More documentation is available at: https://github.com/logstash/logstash-logback-encoder
        val logstashAppender = LogstashTcpSocketAppender()
        logstashAppender.addDestinations(InetSocketAddress(jHipsterProperties.logging.logstash.host,
            jHipsterProperties.logging.logstash.port))
        logstashAppender.context = context
        logstashAppender.encoder = logstashEncoder()
        logstashAppender.name = LOGSTASH_APPENDER_NAME
        logstashAppender.start()

        // Wrap the appender in an Async appender for performance
        val asyncLogstashAppender = AsyncAppender()
        asyncLogstashAppender.context = context
        asyncLogstashAppender.name = ASYNC_LOGSTASH_APPENDER_NAME
        asyncLogstashAppender.queueSize = jHipsterProperties.logging.logstash.queueSize
        asyncLogstashAppender.addAppender(logstashAppender)
        asyncLogstashAppender.start()

        context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME).addAppender(asyncLogstashAppender)
    }

    private fun compositeJsonEncoder(context: LoggerContext): LoggingEventCompositeJsonEncoder {
        val compositeJsonEncoder = LoggingEventCompositeJsonEncoder()
        compositeJsonEncoder.context = context
        compositeJsonEncoder.providers = jsonProviders(context)
        compositeJsonEncoder.start()
        return compositeJsonEncoder
    }

    private fun logstashEncoder(): LogstashEncoder {
        val logstashEncoder = LogstashEncoder()
        logstashEncoder.throwableConverter = throwableConverter()
        logstashEncoder.customFields = customFields()
        return logstashEncoder
    }

    private fun jsonProviders(context: LoggerContext): LoggingEventJsonProviders {
        val jsonProviders = LoggingEventJsonProviders()
        jsonProviders.addArguments(ArgumentsJsonProvider())
        jsonProviders.addContext(ContextJsonProvider())
        jsonProviders.addGlobalCustomFields(customFieldsJsonProvider())
        jsonProviders.addLogLevel(LogLevelJsonProvider())
        jsonProviders.addLoggerName(loggerNameJsonProvider())
        jsonProviders.addMdc(MdcJsonProvider())
        jsonProviders.addMessage(MessageJsonProvider())
        jsonProviders.addPattern(LoggingEventPatternJsonProvider())
        jsonProviders.addStackTrace(stackTraceJsonProvider())
        jsonProviders.addThreadName(ThreadNameJsonProvider())
        jsonProviders.addTimestamp(timestampJsonProvider())
        jsonProviders.setContext(context)
        return jsonProviders
    }

    private fun customFieldsJsonProvider(): GlobalCustomFieldsJsonProvider<ILoggingEvent> {
        val customFieldsJsonProvider = GlobalCustomFieldsJsonProvider<ILoggingEvent>()
        customFieldsJsonProvider.customFields = customFields()
        return customFieldsJsonProvider
    }

    private fun customFields(): String {
        val customFields = StringBuilder()
        customFields.append("{")
        customFields.append("\"app_name\":\"").append(appName).append("\"")
        customFields.append(",").append("\"app_port\":\"").append(this.serverPort).append("\"")
        customFields.append("}")
        return customFields.toString()
    }

    private fun loggerNameJsonProvider(): LoggerNameJsonProvider {
        val loggerNameJsonProvider = LoggerNameJsonProvider()
        loggerNameJsonProvider.shortenedLoggerNameLength = 20
        return loggerNameJsonProvider
    }

    private fun stackTraceJsonProvider(): StackTraceJsonProvider {
        val stackTraceJsonProvider = StackTraceJsonProvider()
        stackTraceJsonProvider.throwableConverter = throwableConverter()
        return stackTraceJsonProvider
    }

    private fun throwableConverter(): ShortenedThrowableConverter {
        val throwableConverter = ShortenedThrowableConverter()
        throwableConverter.isRootCauseFirst = true
        return throwableConverter
    }

    private fun timestampJsonProvider(): LoggingEventFormattedTimestampJsonProvider {
        val timestampJsonProvider = LoggingEventFormattedTimestampJsonProvider()
        timestampJsonProvider.timeZone = "UTC"
        timestampJsonProvider.fieldName = "timestamp"
        return timestampJsonProvider
    }

    private fun addContextListener(context: LoggerContext) {
        val loggerContextListener = LogbackLoggerContextListener(jHipsterProperties)
        loggerContextListener.context = context
        context.addListener(loggerContextListener)
    }

    // Configure a log filter to remove "metrics" logs from all appenders except the "LOGSTASH" appender
    private fun setMetricsMarkerLogbackFilter(context: LoggerContext) {
        log.info("Filtering metrics logs from all appenders except the {} appender", LOGSTASH_APPENDER_NAME)
        val onMarkerMetricsEvaluator = OnMarkerEvaluator()
        onMarkerMetricsEvaluator.context = context
        onMarkerMetricsEvaluator.addMarker("metrics")
        onMarkerMetricsEvaluator.start()
        val metricsFilter = EvaluatorFilter<ILoggingEvent>()
        metricsFilter.context = context
        metricsFilter.evaluator = onMarkerMetricsEvaluator
        metricsFilter.onMatch = FilterReply.DENY
        metricsFilter.start()

        for (logger in context.loggerList) {
            val it = logger.iteratorForAppenders()
            while (it.hasNext()) {
                val appender = it.next()
                if (appender.name != ASYNC_LOGSTASH_APPENDER_NAME &&
                    appender.name != CONSOLE_APPENDER_NAME && jHipsterProperties.logging.isUseJsonFormat) {
                    log.debug("Filter metrics logs from the {} appender", appender.name)
                    appender.context = context
                    appender.addFilter(metricsFilter)
                    appender.start()
                }
            }
        }
    }

    /**
     * Logback configuration is achieved by configuration file and API.
     * When configuration file change is detected, the configuration is reset.
     * This listener ensures that the programmatic configuration is also re-applied after reset.
     */
    internal inner class LogbackLoggerContextListener(
        private val jHipsterProperties: JHipsterProperties
    ) : ContextAwareBase(), LoggerContextListener {

        override fun isResetResistant(): Boolean {
            return true
        }

        override fun onStart(context: LoggerContext) {
            if (jHipsterProperties.logging.isUseJsonFormat) {
                addJsonConsoleAppender(context)
            }
            if (jHipsterProperties.logging.logstash.isEnabled) {
                addLogstashTcpSocketAppender(context)
            }
        }

        override fun onReset(context: LoggerContext) {
            if (jHipsterProperties.logging.isUseJsonFormat) {
                addJsonConsoleAppender(context)
            }
            if (jHipsterProperties.logging.logstash.isEnabled) {
                addLogstashTcpSocketAppender(context)
            }
        }

        override fun onStop(context: LoggerContext) {
            // Nothing to do.
        }

        override fun onLevelChange(logger: ch.qos.logback.classic.Logger, level: Level) {
            // Nothing to do.
        }
    }

    companion object {

        private const val CONSOLE_APPENDER_NAME = "CONSOLE"

        private const val LOGSTASH_APPENDER_NAME = "LOGSTASH"

        private const val ASYNC_LOGSTASH_APPENDER_NAME = "ASYNC_LOGSTASH"
    }
}

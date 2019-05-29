package com.mycompany.myapp.config

import io.github.jhipster.async.ExceptionHandlingAsyncTaskExecutor
import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

import java.util.concurrent.Executor

@Configuration
@EnableAsync
@EnableScheduling
class AsyncConfiguration(private val taskExecutionProperties: TaskExecutionProperties) : AsyncConfigurer {

    private val log = LoggerFactory.getLogger(AsyncConfiguration::class.java)

    @Bean(name = ["taskExecutor"])
    override fun getAsyncExecutor(): Executor {
        log.debug("Creating Async Task Executor")
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = taskExecutionProperties.pool.coreSize
        executor.maxPoolSize = taskExecutionProperties.pool.maxSize
        executor.setQueueCapacity(taskExecutionProperties.pool.queueCapacity)
        executor.threadNamePrefix = taskExecutionProperties.threadNamePrefix
        return ExceptionHandlingAsyncTaskExecutor(executor)
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler {
        return SimpleAsyncUncaughtExceptionHandler()
    }
}

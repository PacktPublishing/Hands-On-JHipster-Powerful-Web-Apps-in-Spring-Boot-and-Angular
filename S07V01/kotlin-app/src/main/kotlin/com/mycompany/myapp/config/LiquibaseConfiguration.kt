package com.mycompany.myapp.config

import io.github.jhipster.config.JHipsterConstants
import io.github.jhipster.config.liquibase.AsyncSpringLiquibase
import liquibase.integration.spring.SpringLiquibase
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles

import javax.sql.DataSource
import java.util.concurrent.Executor

@Configuration
class LiquibaseConfiguration(private val env: Environment, private val cacheManager: CacheManager) {

    private val log = LoggerFactory.getLogger(LiquibaseConfiguration::class.java)

    @Bean
    fun liquibase(
        @Qualifier("taskExecutor") executor: Executor,
        dataSource: DataSource,
        liquibaseProperties: LiquibaseProperties
    ): SpringLiquibase {

        // Use liquibase.integration.spring.SpringLiquibase if you don't want Liquibase to start asynchronously
        val liquibase = AsyncSpringLiquibase(executor, env)
        liquibase.dataSource = dataSource
        liquibase.changeLog = "classpath:config/liquibase/master.xml"
        liquibase.contexts = liquibaseProperties.contexts
        liquibase.defaultSchema = liquibaseProperties.defaultSchema
        liquibase.isDropFirst = liquibaseProperties.isDropFirst
        liquibase.setChangeLogParameters(liquibaseProperties.parameters)
        if (env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_NO_LIQUIBASE))) {
            liquibase.setShouldRun(false)
        } else {
            liquibase.setShouldRun(liquibaseProperties.isEnabled)
            log.debug("Configuring Liquibase")
        }
        return liquibase
    }
}

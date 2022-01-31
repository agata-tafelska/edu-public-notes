package com.atafelska.service.notes.core

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import javax.sql.DataSource

@Configuration
class NotesDatabaseConfiguration(
        @Value("\${spring.datasource.url}") val dbUrl: String,
        @Value("\${spring.datasource.user}") val dbUsername: String,
        @Value("\${spring.datasource.password}") val dbPassword: String,
        @Value("\${spring.datasource.driverClassName}") val dbDriverClassName: String,
        @Value("\${spring.datasource.maxConnections}") val maxConnections: String
) {

    class NotesDatabase(val dataSource: DataSource)

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    fun notesDatabase(dataSource: DataSource) = NotesDatabase(dataSource)

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    fun notesDataSource() =
            HikariDataSource(
                    HikariConfig().apply {
                        jdbcUrl = dbUrl
                        username = dbUsername
                        password = dbPassword
                        driverClassName = dbDriverClassName
                        maximumPoolSize = maxConnections.toInt()
                    })
}

@Configuration
class FlywayMigrationsConfiguration : FlywayAutoConfiguration.FlywayConfiguration()

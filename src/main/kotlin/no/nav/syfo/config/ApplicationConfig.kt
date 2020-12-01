package no.nav.syfo.config

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Primary
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.jta.JtaTransactionManager
import org.springframework.web.client.RestTemplate

@Configuration
@EnableTransactionManagement
@EnableScheduling
@EnableAspectJAutoProxy
class ApplicationConfig {
    // Sørger for at flyway migrering skjer etter at JTA transaction manager er ferdig satt opp av Spring.
    // Forhindrer WARNING: transaction manager not running? loggspam fra Atomikos.
    @Bean
    fun flywayMigrationStrategy(jtaTransactionManager: JtaTransactionManager?): FlywayMigrationStrategy {
        return FlywayMigrationStrategy { obj: Flyway -> obj.migrate() }
    }

    @Bean
    fun taskScheduler(): TaskScheduler {
        return ConcurrentTaskScheduler()
    }

    @Bean
    @Primary
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}

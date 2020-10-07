package no.nav.syfo

import no.nav.security.spring.oidc.api.EnableOIDCTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.runApplication


@SpringBootApplication(exclude = [FlywayAutoConfiguration::class])
@EnableOIDCTokenValidation
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

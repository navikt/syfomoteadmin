package no.nav.syfo

import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.env.Environment
import java.util.*

@Configuration
@Import(TokenGeneratorConfiguration::class)
class LocalApplicationConfig(environment: Environment) {
    init {
        System.setProperty("SECURITYTOKENSERVICE_URL", Objects.requireNonNull(environment.getProperty("securitytokenservice.url")))
        System.setProperty("srv_username", Objects.requireNonNull(environment.getProperty("srv.username")))
        System.setProperty("srv_password", Objects.requireNonNull(environment.getProperty("srv.password")))
        System.setProperty("LDAP_URL", Objects.requireNonNull(environment.getProperty("ldap.url")))
        System.setProperty("LDAP_USERNAME", Objects.requireNonNull(environment.getProperty("ldap.username")))
        System.setProperty("LDAP_PASSWORD", Objects.requireNonNull(environment.getProperty("ldap.password")))
        System.setProperty("LDAP_BASEDN", Objects.requireNonNull(environment.getProperty("ldap.basedn")))
        System.setProperty("SMTPSERVER_HOST", Objects.requireNonNull(environment.getProperty("smtpserver.host")))
        System.setProperty("SMTPSERVER_PORT", Objects.requireNonNull(environment.getProperty("smtpserver.port")))
        System.setProperty("HENVENDELSE_OPPGAVE_HENVENDELSE_QUEUENAME", Objects.requireNonNull(environment.getProperty("henvendelse.oppgave.henvendelse.queuename")))
        System.setProperty("VARSELPRODUKSJON_BEST_VARSEL_M_HANDLING_QUEUENAME", Objects.requireNonNull(environment.getProperty("varselproduksjon.best.varsel.m.handling.queuename")))
        System.setProperty("VARSELPRODUKSJON_STOPP_VARSEL_UTSENDING_QUEUENAME", Objects.requireNonNull(environment.getProperty("varselproduksjon.topp.varsel.utsending.queuename")))
        System.setProperty("VARSELPRODUKSJON_VARSLINGER_QUEUENAME", Objects.requireNonNull(environment.getProperty("varselproduksjon.varslinger.queuename")))
    }
}

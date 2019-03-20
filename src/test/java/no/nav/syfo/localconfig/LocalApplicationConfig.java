package no.nav.syfo.localconfig;

import no.nav.security.spring.oidc.test.TokenGeneratorConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

@Configuration
@Import(TokenGeneratorConfiguration.class)
public class LocalApplicationConfig {

    public LocalApplicationConfig(Environment environment) {
        System.setProperty("SECURITYTOKENSERVICE_URL", requireNonNull(environment.getProperty("securitytokenservice.url")));
        System.setProperty("SRVSYFOMOTEADMIN_USERNAME", requireNonNull(environment.getProperty("srvsyfomoteadmin.username")));
        System.setProperty("SRVSYFOMOTEADMIN_PASSWORD", requireNonNull(environment.getProperty("srvsyfomoteadmin.password")));

        System.setProperty("LDAP_URL", requireNonNull(environment.getProperty("ldap.url")));
        System.setProperty("LDAP_USERNAME", requireNonNull(environment.getProperty("ldap.username")));
        System.setProperty("LDAP_PASSWORD", requireNonNull(environment.getProperty("ldap.password")));
        System.setProperty("LDAP_BASEDN", requireNonNull(environment.getProperty("ldap.basedn")));

        System.setProperty("SMTPSERVER_HOST", requireNonNull(environment.getProperty("smtpserver.host")));
        System.setProperty("SMTPSERVER_PORT", requireNonNull(environment.getProperty("smtpserver.port")));

        System.setProperty("HENVENDELSE_OPPGAVE_HENVENDELSE_QUEUENAME", requireNonNull(environment.getProperty("henvendelse.oppgave.henvendelse.queuename")));
        System.setProperty("VARSELPRODUKSJON_BEST_VARSEL_M_HANDLING_QUEUENAME", requireNonNull(environment.getProperty("varselproduksjon.best.varsel.m.handling.queuename")));
        System.setProperty("VARSELPRODUKSJON_STOPP_VARSEL_UTSENDING_QUEUENAME", requireNonNull(environment.getProperty("varselproduksjon.topp.varsel.utsending.queuename")));
        System.setProperty("VARSELPRODUKSJON_VARSLINGER_QUEUENAME", requireNonNull(environment.getProperty("varselproduksjon.varslinger.queuename")));
    }

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestInterceptor... interceptors) {
        RestTemplate template = new RestTemplate();
        template.setInterceptors(asList(interceptors));
        return template;
    }
}
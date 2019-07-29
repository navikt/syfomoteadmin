package no.nav.syfo.config.consumer;

import no.nav.syfo.service.ws.*;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;

import static java.util.Collections.singletonList;

@Configuration
public class OrganisasjonEnhetConfig {

    public static final String MOCK_KEY = "organisasjonenhet.withmock";
    @Value("${virksomhet.organisasjonEnhet.v2.endpointurl}")
    private String serviceUrl;

    @SuppressWarnings("unchecked")
    @Bean
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    @Primary
    public OrganisasjonEnhetV2 organisasjonEnhetV2() {
        OrganisasjonEnhetV2 port = new WsClient<OrganisasjonEnhetV2>().createPort(serviceUrl, OrganisasjonEnhetV2.class, singletonList(new LogErrorHandler()));
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }
}
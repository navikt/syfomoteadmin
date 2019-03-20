package no.nav.syfo.config.consumer;

import no.nav.syfo.service.ws.LogErrorHandler;
import no.nav.syfo.service.ws.STSClientConfig;
import no.nav.syfo.service.ws.WsClient;
import no.nav.tjeneste.virksomhet.person.v3.PersonV3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static java.util.Collections.singletonList;

@Configuration
public class PersonConfig {

    public static final String MOCK_KEY = "person.withmock";

    @SuppressWarnings("unchecked")
    @Bean
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    @Primary
    public PersonV3 personV3(@Value("${virksomhet.person.v3.endpointurl}") String serviceUrl) {
        PersonV3 port = new WsClient<PersonV3>().createPort(serviceUrl, PersonV3.class, singletonList(new LogErrorHandler()));
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }
}
package no.nav.syfo.config.consumer;

import no.nav.syfo.service.ws.*;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;

import java.util.Collections;

@Configuration
public class ArbeidsfordelingConfig {

    public static final String MOCK_KEY = "arbeidsfordeling.withmock";
    @Value("${virksomhet.arbeidsfordeling.v1.endpointurl}")
    private String serviceUrl;

    @SuppressWarnings("unchecked")
    @Bean
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    @Primary
    public ArbeidsfordelingV1 arbeidsfordelingV1() {
        ArbeidsfordelingV1 port = new WsClient<ArbeidsfordelingV1>().createPort(serviceUrl, ArbeidsfordelingV1.class, Collections.singletonList(new LogErrorHandler()));
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }
}

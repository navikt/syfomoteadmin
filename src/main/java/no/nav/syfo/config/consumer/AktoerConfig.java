package no.nav.syfo.config.consumer;

import no.nav.syfo.service.ws.LogErrorHandler;
import no.nav.syfo.service.ws.STSClientConfig;
import no.nav.syfo.service.ws.WsClient;
import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static java.util.Collections.singletonList;

@Configuration
public class AktoerConfig {

    public static final String MOCK_KEY = "aktoer.withmock";
    @Value("${aktoer.v2.endpointurl}")
    private String serviceUrl;

    @Bean
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    @Primary
    public AktoerV2 aktoerV2() {
        AktoerV2 port = factory();
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }

    @SuppressWarnings("unchecked")
    private AktoerV2 factory() {
        return new WsClient<AktoerV2>()
                .createPort(serviceUrl, AktoerV2.class, singletonList(new LogErrorHandler()));
    }
}

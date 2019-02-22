package no.nav.syfo.config.consumer;

import no.nav.syfo.service.ws.LogErrorHandler;
import no.nav.syfo.service.ws.STSClientConfig;
import no.nav.syfo.service.ws.WsClient;
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static java.util.Collections.singletonList;

@Configuration
public class EgenAnsattConfig {

    public static final String MOCK_KEY = "egenansatt.withmock";
    @Value("${virksomhet.egenansatt.v1.endpointurl}")
    private String serviceUrl;

    @Bean
    @Primary
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    public EgenAnsattV1 egenAnsattV1() {
        EgenAnsattV1 port = factory();
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }

    @SuppressWarnings("unchecked")
    private EgenAnsattV1 factory() {
        return new WsClient<EgenAnsattV1>()
                .createPort(serviceUrl, EgenAnsattV1.class, singletonList(new LogErrorHandler()));
    }
}

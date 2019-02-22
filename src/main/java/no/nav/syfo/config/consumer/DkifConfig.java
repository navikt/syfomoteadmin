package no.nav.syfo.config.consumer;

import no.nav.syfo.service.ws.LogErrorHandler;
import no.nav.syfo.service.ws.STSClientConfig;
import no.nav.syfo.service.ws.WsClient;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static java.util.Collections.singletonList;

@Configuration
public class DkifConfig {

    public static final String MOCK_KEY = "arena.withmock";
    @Value("${virksomhet.digitalkontakinformasjon.v1.endpointurl}")
    private String serviceUrl;

    @Bean
    @Primary
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    public DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1() {
        DigitalKontaktinformasjonV1 port = factory();
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }

    @SuppressWarnings("unchecked")
    private DigitalKontaktinformasjonV1 factory() {
        return new WsClient<DigitalKontaktinformasjonV1>()
                .createPort(serviceUrl, DigitalKontaktinformasjonV1.class, singletonList(new LogErrorHandler()));
    }
}

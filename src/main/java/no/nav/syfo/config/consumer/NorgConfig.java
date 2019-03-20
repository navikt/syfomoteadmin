package no.nav.syfo.config.consumer;

import no.nav.syfo.service.ws.LogErrorHandler;
import no.nav.syfo.service.ws.STSClientConfig;
import no.nav.syfo.service.ws.WsClient;
import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.OrganisasjonRessursEnhetV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static java.util.Collections.singletonList;

@Configuration
public class NorgConfig {

    public static final String MOCK_KEY = "norg.withmock";
    @Value("${virksomhet.organisasjonressursenhet.v1.endpointurl}")
    private String serviceUrl;

    @Bean
    @Primary
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    public OrganisasjonRessursEnhetV1 organisasjonRessursEnhetV1() {
        OrganisasjonRessursEnhetV1 port = factory();
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }

    @SuppressWarnings("unchecked")
    private OrganisasjonRessursEnhetV1 factory() {
        return new WsClient<OrganisasjonRessursEnhetV1>()
                .createPort(serviceUrl, OrganisasjonRessursEnhetV1.class, singletonList(new LogErrorHandler()));
    }
}

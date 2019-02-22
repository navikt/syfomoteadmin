package no.nav.syfo.config.consumer;

import no.nav.syfo.service.ws.LogErrorHandler;
import no.nav.syfo.service.ws.STSClientConfig;
import no.nav.syfo.service.ws.WsClient;
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.BehandleArbeidOgAktivitetOppgaveV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static java.util.Collections.singletonList;

@Configuration
public class ArenaConfig {

    public static final String MOCK_KEY = "arena.withmock";
    @Value("${virksomhet.behandlearbeidogaktivitetoppgave.v1.endpointurl}")
    private String serviceUrl;

    @Bean
    @Primary
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    public BehandleArbeidOgAktivitetOppgaveV1 behandleArbeidOgAktivitetOppgaveV1() {
        BehandleArbeidOgAktivitetOppgaveV1 port = factory();
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }

    @SuppressWarnings("unchecked")
    private BehandleArbeidOgAktivitetOppgaveV1 factory() {
        return new WsClient<BehandleArbeidOgAktivitetOppgaveV1>()
                .createPort(serviceUrl, BehandleArbeidOgAktivitetOppgaveV1.class, singletonList(new LogErrorHandler()));
    }
}

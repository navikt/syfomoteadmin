package no.nav.syfo.config.consumer;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.syfo.config.mocks.ArenaMock;
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.BehandleArbeidOgAktivitetOppgaveV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;

@Configuration
public class ArenaConfig {

    private static final String ENDEPUNKT_URL = getProperty("VIRKSOMHET_BEHANDLEARBEIDOGAKTIVITETOPPGAVE_V1_ENDPOINTURL");

    @Bean
    public BehandleArbeidOgAktivitetOppgaveV1 behandleArbeidOgAktivitetOppgaveV1() {
        BehandleArbeidOgAktivitetOppgaveV1 prod = factory()
                .configureStsForOnBehalfOfWithJWT()
                .build();
        BehandleArbeidOgAktivitetOppgaveV1 mock = new ArenaMock();

        return createMetricsProxyWithInstanceSwitcher("BehandleArbeidOgAktivitetOppgaveV1", prod, mock, "tillatmock", BehandleArbeidOgAktivitetOppgaveV1.class);
    }

    private CXFClient<BehandleArbeidOgAktivitetOppgaveV1> factory() {
        return new CXFClient<>(BehandleArbeidOgAktivitetOppgaveV1.class).address(ENDEPUNKT_URL);
    }
}

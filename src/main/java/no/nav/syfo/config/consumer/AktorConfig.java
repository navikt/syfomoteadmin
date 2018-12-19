package no.nav.syfo.config.consumer;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.syfo.config.mocks.AktoerMock;
import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class AktorConfig {

    private static final String MOCK_KEY = "aktoer.withmock";
    private static final String ENDEPUNKT_URL = getProperty("AKTOER_V2_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "AKTOER_V2";
    private static final boolean KRITISK = true;

    @Bean
    public AktoerV2 aktoerV1() {
        AktoerV2 prod = factory()
                .configureStsForOnBehalfOfWithJWT()
                .build();
        AktoerV2 mock = new AktoerMock();
        return createMetricsProxyWithInstanceSwitcher(ENDEPUNKT_NAVN, prod, mock, MOCK_KEY, AktoerV2.class);
    }

    @Bean
    public Pingable aktoerPing() {
        Pingable.Ping.PingMetadata pingMetadata = new Pingable.Ping.PingMetadata(ENDEPUNKT_URL, ENDEPUNKT_NAVN, KRITISK);
        final AktoerV2 aktoerPing = factory()
                .configureStsForSystemUserInFSS()
                .build();
        return () -> {
            try {
                aktoerPing.ping();
                return lyktes(pingMetadata);
            } catch (Exception e) {
                return feilet(pingMetadata, e);
            }
        };
    }

    private CXFClient<AktoerV2> factory() {
        return new CXFClient<>(AktoerV2.class)
                .address(ENDEPUNKT_URL);
    }
}

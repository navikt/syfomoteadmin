package no.nav.syfo.config.consumer;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.syfo.config.mocks.BrukerProfilV3Mock;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.BrukerprofilV3;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class BrukerprofilConfig {

    private static final String MOCK_KEY = "brukerprofil.withmock";
    private static final String ENDEPUNKT_URL = getProperty("VIRKSOMHET_BRUKERPROFIL_V3_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "BRUKERPROFIL_V3";
    private static final boolean KRITISK = true;

    @Bean
    public BrukerprofilV3 brukerprofilV3() {
        BrukerprofilV3 prod = factory()
                .configureStsForOnBehalfOfWithJWT()
                .build();
        BrukerprofilV3 mock = new BrukerProfilV3Mock();
        return createMetricsProxyWithInstanceSwitcher(ENDEPUNKT_NAVN, prod, mock, MOCK_KEY, BrukerprofilV3.class);
    }

    @Bean
    public Pingable brukerprofilV3Ping() {
        PingMetadata pingMetadata = new PingMetadata(
                UUID.randomUUID().toString(),
                ENDEPUNKT_URL,
                ENDEPUNKT_NAVN,
                KRITISK
        );
        return () -> {
            try {
                factory()
                        .configureStsForSystemUser()
                        .build();
                return lyktes(pingMetadata);
            } catch (Exception e) {
                return feilet(pingMetadata, e);
            }
        };
    }

    private CXFClient<BrukerprofilV3> factory() {
        return new CXFClient<>(BrukerprofilV3.class)
                .address(ENDEPUNKT_URL);
    }
}

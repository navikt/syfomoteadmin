package no.nav.syfo.config.consumer;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.syfo.config.mocks.DkifMock;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class DkifConfig {

    private static final String ENDEPUNKT_URL = getProperty("VIRKSOMHET_DIGITALKONTAKINFORMASJON_V1_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "DIGITALKONTAKTINFORMASJON_V1";
    private static final boolean KRITISK = true;

    @Bean
    public DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1() {
        DigitalKontaktinformasjonV1 prod = factory()
                .configureStsForOnBehalfOfWithJWT()
                .build();
        DigitalKontaktinformasjonV1 mock = new DkifMock();
        return createMetricsProxyWithInstanceSwitcher(ENDEPUNKT_NAVN, prod, mock, "tillatmock", DigitalKontaktinformasjonV1.class);
    }

    @Bean
    public Pingable dkifV1Ping() {
        Pingable.Ping.PingMetadata pingMetadata = new Pingable.Ping.PingMetadata(ENDEPUNKT_URL, ENDEPUNKT_NAVN, KRITISK);
        return () -> {
            try {
                factory().configureStsForSystemUserInFSS()
                        .build().ping();
                return lyktes(pingMetadata);
            } catch (Exception e) {
                return feilet(pingMetadata, e);
            }
        };
    }

    private CXFClient<DigitalKontaktinformasjonV1> factory() {
        return new CXFClient<>(DigitalKontaktinformasjonV1.class)
                .address(ENDEPUNKT_URL);
    }
}

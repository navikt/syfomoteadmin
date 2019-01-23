package no.nav.syfo.config.consumer;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.syfo.config.mocks.SykefravaersoppfoelgingV1Mock;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.SykefravaersoppfoelgingV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class SykefravaersoppfoelgingConfig {

    private static final String MOCK_KEY = "sykefravaersoppfoelging.withmock";
    private static final String ENDEPUNKT_URL = getProperty("SYKEFRAVAERSOPPFOELGING_V1_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "SYKEFRAVAERSOPPFOELGING_V1";
    private static final boolean KRITISK = true;

    @Bean(name = "sykefravaersoppfoelgingV1")
    public SykefravaersoppfoelgingV1 sykefravaersoppfoelgingV1() {
        SykefravaersoppfoelgingV1 prod = factory()
                .configureStsForOnBehalfOfWithJWT()
                .build();
        SykefravaersoppfoelgingV1 mock = new SykefravaersoppfoelgingV1Mock();
        return createMetricsProxyWithInstanceSwitcher(ENDEPUNKT_NAVN, prod, mock, MOCK_KEY, SykefravaersoppfoelgingV1.class);
    }

    @Bean(name = "sykefravaersoppfoelgingV1SystemBruker")
    public SykefravaersoppfoelgingV1 sykefravaersoppfoelgingV1Systembruker() {
        SykefravaersoppfoelgingV1 prod = factory()
                .configureStsForSystemUser()
                .build();
        SykefravaersoppfoelgingV1 mock = new SykefravaersoppfoelgingV1Mock();
        return createMetricsProxyWithInstanceSwitcher(ENDEPUNKT_NAVN, prod, mock, MOCK_KEY, SykefravaersoppfoelgingV1.class);
    }

    @Bean
    public Pingable sykefravaersoppfoelgingV1Ping() {
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

    private CXFClient<SykefravaersoppfoelgingV1> factory() {
        return new CXFClient<>(SykefravaersoppfoelgingV1.class)
                .address(ENDEPUNKT_URL);
    }
}

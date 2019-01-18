package no.nav.syfo.config.consumer;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.syfo.config.mocks.NorgMock;
import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.OrganisasjonRessursEnhetV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class NorgConfig {

    private static final String ENDEPUNKT_URL = getProperty("VIRKSOMHET_ORGANISASJONRESSURSENHET_V1_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "ORGANISASJONRESSURSENHET_V1";
    private static final boolean KRITISK = true;

    @Bean
    public OrganisasjonRessursEnhetV1 organisasjonRessursEnhetV1() {
        OrganisasjonRessursEnhetV1 prod = factory()
                .configureStsForOnBehalfOfWithJWT()
                .build();
        OrganisasjonRessursEnhetV1 mock = new NorgMock();

        return createMetricsProxyWithInstanceSwitcher(ENDEPUNKT_NAVN, prod, mock, "tillatmock", OrganisasjonRessursEnhetV1.class);
    }

    @Bean
    public Pingable organisasjonRessursEnhetV1Ping() {
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
                        .build().ping();
                return lyktes(pingMetadata);
            } catch (Exception e) {
                return feilet(pingMetadata, e);
            }
        };
    }

    private CXFClient<OrganisasjonRessursEnhetV1> factory() {
        return new CXFClient<>(OrganisasjonRessursEnhetV1.class)
                .address(ENDEPUNKT_URL);
    }
}

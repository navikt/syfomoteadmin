package no.nav.syfo.config.consumer;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.syfo.config.mocks.PersonV3Mock;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;


@Configuration
public class PersonConfig {

    private static final String MOCK_KEY = "PERSON_WITHMOCK";
    private static final String ENDEPUNKT_URL = getProperty("VIRKSOMHET_PERSON_V3_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "PERSON_V3";
    private static final boolean KRITISK = true;

    @Bean
    public PersonV3 personV3() {
        PersonV3 prod = factory()
                .configureStsForOnBehalfOfWithJWT()
                .build();
        PersonV3 mock = new PersonV3Mock();
        return createMetricsProxyWithInstanceSwitcher(ENDEPUNKT_NAVN, prod, mock, MOCK_KEY, PersonV3.class);
    }

    @Bean
    public Pingable personV3Ping() {
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

    private CXFClient<PersonV3> factory() {
        return new CXFClient<>(PersonV3.class)
                .address(ENDEPUNKT_URL);
    }
}

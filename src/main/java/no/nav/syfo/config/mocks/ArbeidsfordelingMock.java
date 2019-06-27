package no.nav.syfo.config.mocks;

import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.WSEnhetsstatus;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.WSOrganisasjonsenhet;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static java.util.Arrays.asList;
import static no.nav.syfo.config.consumer.ArbeidsfordelingConfig.MOCK_KEY;

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
public class ArbeidsfordelingMock implements ArbeidsfordelingV1 {

    @Override
    public void ping() {
    }

    @Override
    public WSFinnAlleBehandlendeEnheterListeResponse
    finnAlleBehandlendeEnheterListe(WSFinnAlleBehandlendeEnheterListeRequest request) {
        return new WSFinnAlleBehandlendeEnheterListeResponse().withBehandlendeEnhetListe(asList(
                new WSOrganisasjonsenhet()
                        .withEnhetId("0330")
                        .withEnhetNavn("NAV Bjerke")
                        .withStatus(
                                WSEnhetsstatus.fromValue("AKTIV")
                        ),
                new WSOrganisasjonsenhet()
                        .withEnhetNavn("0314")
                        .withEnhetNavn("NAV Sagene")
                        .withStatus(
                                WSEnhetsstatus.fromValue("AKTIV")
                        )
        ));
    }

    @Override
    public WSFinnBehandlendeEnhetListeResponse
    finnBehandlendeEnhetListe(WSFinnBehandlendeEnhetListeRequest request) {
        return new WSFinnBehandlendeEnhetListeResponse().withBehandlendeEnhetListe(asList(
                new WSOrganisasjonsenhet()
                        .withEnhetId("0330")
                        .withEnhetNavn("NAV Bjerke")
                        .withStatus(
                                WSEnhetsstatus.fromValue("AKTIV")
                        ),
                new WSOrganisasjonsenhet()
                        .withEnhetNavn("0314")
                        .withEnhetNavn("NAV Sagene")
                        .withStatus(
                                WSEnhetsstatus.fromValue("AKTIV")
                        )
        ));
    }
}

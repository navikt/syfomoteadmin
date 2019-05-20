package no.nav.syfo.config.mocks;

import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static no.nav.syfo.config.consumer.NorgConfig.MOCK_KEY;

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
public class NorgMock implements OrganisasjonRessursEnhetV1 {

    private static final String NAV_ENHET = "0330";
    public static final String NAV_ENHET_NAVN = "navEnhet";

    @Override
    public WSHentEnhetListeResponse hentEnhetListe(WSHentEnhetListeRequest request) throws HentEnhetListeUgyldigInput, HentEnhetListeRessursIkkeFunnet {
        return new WSHentEnhetListeResponse().withEnhetListe(new WSEnhet().withEnhetId(NAV_ENHET).withNavn(NAV_ENHET_NAVN));
    }

    @Override
    public WSHentRessursIdListeResponse hentRessursIdListe(WSHentRessursIdListeRequest request) throws HentRessursIdListeEnhetikkefunnet, HentRessursIdListeUgyldigInput {
        return null;
    }

    @Override
    public void ping() {

    }
}

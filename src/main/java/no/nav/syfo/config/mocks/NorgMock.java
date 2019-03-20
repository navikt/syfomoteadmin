package no.nav.syfo.config.mocks;

import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static no.nav.syfo.config.consumer.NorgConfig.MOCK_KEY;

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
public class NorgMock implements OrganisasjonRessursEnhetV1 {

    @Override
    public WSHentEnhetListeResponse hentEnhetListe(WSHentEnhetListeRequest request) throws HentEnhetListeUgyldigInput, HentEnhetListeRessursIkkeFunnet {
        return new WSHentEnhetListeResponse().withEnhetListe(new WSEnhet().withEnhetId("0330").withNavn("navEnhet"));
    }

    @Override
    public WSHentRessursIdListeResponse hentRessursIdListe(WSHentRessursIdListeRequest request) throws HentRessursIdListeEnhetikkefunnet, HentRessursIdListeUgyldigInput {
        return null;
    }

    @Override
    public void ping() {

    }
}

package no.nav.syfo.config.mocks;

import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.*;

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

package no.nav.syfo.config.mocks;


import no.nav.tjeneste.virksomhet.organisasjon.v4.*;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.WSOrganisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.WSUstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.*;

public class OrganisasjonMock implements OrganisasjonV4 {

    @Override
    public WSFinnOrganisasjonResponse finnOrganisasjon(WSFinnOrganisasjonRequest request) throws FinnOrganisasjonForMangeForekomster, FinnOrganisasjonUgyldigInput {
        throw new RuntimeException("Ikke implementert i mock. Se OrganisasjonMock");
    }

    @Override
    public WSHentOrganisasjonsnavnBolkResponse hentOrganisasjonsnavnBolk(WSHentOrganisasjonsnavnBolkRequest request) {
        throw new RuntimeException("Ikke implementert i mock. Se OrganisasjonMock");
    }

    @Override
    public WSHentOrganisasjonResponse hentOrganisasjon(WSHentOrganisasjonRequest request) throws HentOrganisasjonOrganisasjonIkkeFunnet, HentOrganisasjonUgyldigInput {
        return new WSHentOrganisasjonResponse()
                .withOrganisasjon(new WSOrganisasjon()
                        .withNavn(new WSUstrukturertNavn()
                                .withNavnelinje("Testbedrift")
                                .withNavnelinje("Testveien")));
    }

    @Override
    public WSHentNoekkelinfoOrganisasjonResponse hentNoekkelinfoOrganisasjon(WSHentNoekkelinfoOrganisasjonRequest request)
            throws HentNoekkelinfoOrganisasjonOrganisasjonIkkeFunnet, HentNoekkelinfoOrganisasjonUgyldigInput {
        throw new RuntimeException("Ikke implementert i mock. Se OrganisasjonMock");
    }

    @Override
    public WSValiderOrganisasjonResponse validerOrganisasjon(WSValiderOrganisasjonRequest request) throws ValiderOrganisasjonOrganisasjonIkkeFunnet, ValiderOrganisasjonUgyldigInput {
        throw new RuntimeException("Ikke implementert i mock. Se OrganisasjonMock");
    }

    @Override
    public WSHentVirksomhetsOrgnrForJuridiskOrgnrBolkResponse hentVirksomhetsOrgnrForJuridiskOrgnrBolk(WSHentVirksomhetsOrgnrForJuridiskOrgnrBolkRequest request) {
        throw new RuntimeException("Ikke implementert i mock. Se OrganisasjonMock");
    }

    @Override
    public WSFinnOrganisasjonsendringerListeResponse finnOrganisasjonsendringerListe(WSFinnOrganisasjonsendringerListeRequest request)
            throws FinnOrganisasjonsendringerListeUgyldigInput {
        throw new RuntimeException("Ikke implementert i mock. Se OrganisasjonMock");
    }

    @Override
    public void ping() {
    }
}

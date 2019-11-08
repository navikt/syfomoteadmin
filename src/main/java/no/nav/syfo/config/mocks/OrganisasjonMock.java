package no.nav.syfo.config.mocks;


import no.nav.tjeneste.virksomhet.organisasjon.v4.*;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.WSOrganisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.WSUstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static no.nav.syfo.config.consumer.EregConfig.MOCK_KEY;

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
public class OrganisasjonMock implements OrganisasjonV4 {

    public final static String VIRKSOMHET_NAME1 = "Testbedrift";
    public final static String VIRKSOMHET_NAME2 = "Testveien";

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
                                .withNavnelinje(VIRKSOMHET_NAME1)
                                .withNavnelinje(VIRKSOMHET_NAME2)));
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

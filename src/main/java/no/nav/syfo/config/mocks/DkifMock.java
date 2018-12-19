package no.nav.syfo.config.mocks;

import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSEpostadresse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSMobiltelefonnummer;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.*;

import java.time.OffsetDateTime;

public class DkifMock implements DigitalKontaktinformasjonV1 {

    @Override
    public WSHentSikkerDigitalPostadresseBolkResponse hentSikkerDigitalPostadresseBolk(WSHentSikkerDigitalPostadresseBolkRequest request) throws HentSikkerDigitalPostadresseBolkForMangeForespoersler, HentSikkerDigitalPostadresseBolkSikkerhetsbegrensing {
        return null;
    }

    @Override
    public WSHentPrintsertifikatResponse hentPrintsertifikat(WSHentPrintsertifikatRequest request) {
        return null;
    }

    @Override
    public void ping() {

    }

    @Override
    public WSHentSikkerDigitalPostadresseResponse hentSikkerDigitalPostadresse(WSHentSikkerDigitalPostadresseRequest request) throws HentSikkerDigitalPostadresseKontaktinformasjonIkkeFunnet, HentSikkerDigitalPostadresseSikkerhetsbegrensing, HentSikkerDigitalPostadressePersonIkkeFunnet {
        return null;
    }

    @Override
    public WSHentDigitalKontaktinformasjonBolkResponse hentDigitalKontaktinformasjonBolk(WSHentDigitalKontaktinformasjonBolkRequest request) throws HentDigitalKontaktinformasjonBolkForMangeForespoersler, HentDigitalKontaktinformasjonBolkSikkerhetsbegrensing {
        return null;
    }

    @Override
    public WSHentDigitalKontaktinformasjonResponse hentDigitalKontaktinformasjon(WSHentDigitalKontaktinformasjonRequest request) throws HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet, HentDigitalKontaktinformasjonSikkerhetsbegrensing, HentDigitalKontaktinformasjonPersonIkkeFunnet {
        return new WSHentDigitalKontaktinformasjonResponse()
                .withDigitalKontaktinformasjon(new WSKontaktinformasjon()
                        .withEpostadresse(new WSEpostadresse()
                                .withValue("test@nav.no")
                                .withSistVerifisert(OffsetDateTime.now().minusMonths(10)))
                        .withMobiltelefonnummer(new WSMobiltelefonnummer()
                                .withValue("12345678")
                                .withSistVerifisert(OffsetDateTime.now().minusMonths(10)))
                        .withReservasjon("false"));
    }
}

package no.nav.syfo.config.mocks;

import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSEpostadresse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSMobiltelefonnummer;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

import static no.nav.syfo.config.consumer.DkifConfig.MOCK_KEY;

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
public class DkifMock implements DigitalKontaktinformasjonV1 {

    public final static String PERSON_EMAIL = "test@nav.no";
    public final static String PERSON_TLF = "12345678";
    public final static String PERSON_RESERVASJON = "false";

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
                                .withValue(PERSON_EMAIL)
                                .withSistVerifisert(OffsetDateTime.now().minusMonths(10)))
                        .withMobiltelefonnummer(new WSMobiltelefonnummer()
                                .withValue(PERSON_TLF)
                                .withSistVerifisert(OffsetDateTime.now().minusMonths(10)))
                        .withReservasjon(PERSON_RESERVASJON));
    }
}

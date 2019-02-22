package no.nav.syfo.config.mocks;

import no.nav.tjeneste.virksomhet.brukerprofil.v3.BrukerprofilV3;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.HentKontaktinformasjonOgPreferanserPersonIdentErUtgaatt;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.informasjon.WSBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.informasjon.WSDiskresjonskoder;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.informasjon.WSPersonnavn;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.meldinger.WSHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.meldinger.WSHentKontaktinformasjonOgPreferanserResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static no.nav.syfo.config.consumer.BrukerprofilConfig.MOCK_KEY;

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
public class BrukerProfilV3Mock implements BrukerprofilV3 {

    public final static String PERSON_FORNAVN = "Fornavn";
    public final static String PERSON_ETTERNAVN = "Etternavn";

    @Override
    public void ping() {

    }

    @Override
    public WSHentKontaktinformasjonOgPreferanserResponse hentKontaktinformasjonOgPreferanser(WSHentKontaktinformasjonOgPreferanserRequest request) throws HentKontaktinformasjonOgPreferanserPersonIdentErUtgaatt, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        return new WSHentKontaktinformasjonOgPreferanserResponse().withBruker(new WSBruker()
                .withPersonnavn(new WSPersonnavn()
                        .withFornavn(PERSON_FORNAVN)
                        .withEtternavn(PERSON_ETTERNAVN))
                .withDiskresjonskode(new WSDiskresjonskoder().withValue("2")));
    }
}

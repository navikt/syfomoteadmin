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

public class BrukerProfilV3Mock implements BrukerprofilV3 {
    @Override
    public void ping() {

    }

    @Override
    public WSHentKontaktinformasjonOgPreferanserResponse hentKontaktinformasjonOgPreferanser(WSHentKontaktinformasjonOgPreferanserRequest request) throws HentKontaktinformasjonOgPreferanserPersonIdentErUtgaatt, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        return new WSHentKontaktinformasjonOgPreferanserResponse().withBruker(new WSBruker()
                .withPersonnavn(new WSPersonnavn()
                        .withFornavn("Fornavn")
                        .withEtternavn("Etternavn"))
                .withDiskresjonskode(new WSDiskresjonskoder().withValue("2")));
    }
}

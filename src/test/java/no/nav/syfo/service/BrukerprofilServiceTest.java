package no.nav.syfo.service;

import no.nav.syfo.domain.model.TpsPerson;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.BrukerprofilV3;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.HentKontaktinformasjonOgPreferanserPersonIdentErUtgaatt;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.informasjon.WSBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.informasjon.WSDiskresjonskoder;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.informasjon.WSPersonnavn;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.meldinger.WSHentKontaktinformasjonOgPreferanserResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BrukerprofilServiceTest {

    @Mock
    private BrukerprofilV3 brukerprofilV3;
    @Mock
    private AktoerService aktoerService;
    @InjectMocks
    private BrukerprofilService brukerprofilService;

    @Before
    public void setup() {
    }

    @Test
    public void kode6BrukerGirNavnIkkeFunnet() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIdentErUtgaatt, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        when(brukerprofilV3.hentKontaktinformasjonOgPreferanser(any())).thenReturn(new WSHentKontaktinformasjonOgPreferanserResponse()
                .withBruker(new WSBruker()
                        .withDiskresjonskode(new WSDiskresjonskoder()
                                .withValue("6"))));
        TpsPerson s = brukerprofilService.hentBruker("12345678901");
        assertThat(s.navn).isEqualTo("skjermet bruker");
        assertThat(s.skjermetBruker).isTrue();
    }

    @Test
    public void fangerExcveptionOgGirNavnIkkeFunnet() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIdentErUtgaatt, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        when(brukerprofilV3.hentKontaktinformasjonOgPreferanser(any())).thenThrow(new HentKontaktinformasjonOgPreferanserPersonIkkeFunnet());
        String s = brukerprofilService.hentBruker("12345678901").navn;
        assertThat(s).isEqualTo("Vi fant ikke navnet");
    }

    @Test
    public void setterSammenNavnet() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIdentErUtgaatt, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        when(brukerprofilV3.hentKontaktinformasjonOgPreferanser(any())).thenReturn(new WSHentKontaktinformasjonOgPreferanserResponse()
                .withBruker(new WSBruker()
                        .withPersonnavn(new WSPersonnavn()
                                .withFornavn("Fornavn")
                                .withMellomnavn("Mellomnavn")
                                .withEtternavn("Etternavn"))
                )
        );
        String s = brukerprofilService.hentBruker("12345678901").navn;
        assertThat(s).isEqualTo("Fornavn Mellomnavn Etternavn");
    }


    @Test(expected = RuntimeException.class)
    public void kasterRuntimeExceptionDersomManIkkesporOmmEtFnr() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIdentErUtgaatt, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        brukerprofilService.hentBruker(null);
    }

}

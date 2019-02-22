package no.nav.syfo.api.ressurser;

import no.nav.syfo.api.domain.RSBruker;
import no.nav.syfo.domain.model.Kontaktinfo;
import no.nav.syfo.domain.model.TpsPerson;
import no.nav.syfo.service.BrukerprofilService;
import no.nav.syfo.service.DkifService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import static no.nav.syfo.testhelper.OidcTestHelper.loggInnVeileder;
import static no.nav.syfo.testhelper.UserConstants.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

public class BrukerRessursTilgangTest extends AbstractRessursTilgangTest {

    private final TpsPerson tpsPerson = new TpsPerson()
            .navn(PERSON_NAVN)
            .skjermetBruker(false);
    @Inject
    private BrukerRessurs brukerRessurs;
    @Mock
    private BrukerprofilService brukerprofilService;
    @Mock
    private DkifService dkifService;

    @Before
    public void setup() {
        loggInnVeileder(oidcRequestContextHolder, VEILEDER_ID);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);
    }

    @Test
    public void hentBruker_har_tilgang() {
        mockSvarFraTilgangTilBruker(ARBEIDSTAKER_FNR, OK);

        RSBruker bruker = brukerRessurs.hentBruker(ARBEIDSTAKER_FNR);

        assertEquals(tpsPerson.navn, bruker.navn);
    }

    @Test(expected = ForbiddenException.class)
    public void hentBruker_har_ikke_tilgang() {
        mockSvarFraTilgangTilBruker(ARBEIDSTAKER_FNR, FORBIDDEN);

        brukerRessurs.hentBruker(ARBEIDSTAKER_FNR);
    }

    @Test(expected = RuntimeException.class)
    public void hentBruker_annen_tilgangsfeil() {
        mockSvarFraTilgangTilBruker(ARBEIDSTAKER_FNR, INTERNAL_SERVER_ERROR);

        brukerRessurs.hentBruker(ARBEIDSTAKER_FNR);
    }

    @Test
    public void bruker_har_tilgang() {
        mockSvarFraTilgangTilBruker(ARBEIDSTAKER_FNR, OK);

        Kontaktinfo kontaktinfo = new Kontaktinfo().tlf("12345678").skalHaVarsel(true);
        when(dkifService.hentKontaktinfoFnr(ARBEIDSTAKER_FNR)).thenReturn(kontaktinfo);

        RSBruker bruker = brukerRessurs.bruker(ARBEIDSTAKER_FNR);

        assertEquals(kontaktinfo.tlf, bruker.kontaktinfo.tlf);
        assertEquals(tpsPerson.navn, bruker.navn);
    }

    @Test(expected = ForbiddenException.class)
    public void bruker_har_ikke_tilgang() {
        mockSvarFraTilgangTilBruker(ARBEIDSTAKER_FNR, FORBIDDEN);

        brukerRessurs.bruker(ARBEIDSTAKER_FNR);
    }

    @Test(expected = RuntimeException.class)
    public void bruker_annen_tilgangsfeil() {
        mockSvarFraTilgangTilBruker(ARBEIDSTAKER_FNR, INTERNAL_SERVER_ERROR);

        brukerRessurs.bruker(ARBEIDSTAKER_FNR);
    }
}

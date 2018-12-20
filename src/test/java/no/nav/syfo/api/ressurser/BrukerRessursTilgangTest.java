package no.nav.syfo.api.ressurser;

import no.nav.syfo.api.domain.RSBruker;
import no.nav.syfo.domain.model.Kontaktinfo;
import no.nav.syfo.domain.model.TpsPerson;
import no.nav.syfo.service.BrukerprofilService;
import no.nav.syfo.service.DkifService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.ws.rs.ForbiddenException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class BrukerRessursTilgangTest extends AbstractRessursTilgangTest {

    private final TpsPerson tpsPerson = new TpsPerson().navn("Test");
    @Mock
    private BrukerprofilService brukerprofilService;
    @Mock
    private DkifService dkifService;
    @InjectMocks
    private BrukerRessurs brukerRessurs;

    @Test
    public void hentBruker_har_tilgang() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(200);
        when(brukerprofilService.hentBruker(FNR)).thenReturn(tpsPerson);

        RSBruker bruker = brukerRessurs.hentBruker(FNR);

        assertEquals(tpsPerson.navn, bruker.navn);

        verify(tilgangskontrollResponse, times(2)).getStatus();
        verify(brukerprofilService).hentBruker(FNR);
    }

    @Test(expected = ForbiddenException.class)
    public void hentBruker_har_ikke_tilgang() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(403);

        brukerRessurs.hentBruker(FNR);

        verify(tilgangskontrollResponse).getStatus();
    }

    @Test(expected = RuntimeException.class)
    public void hentBruker_annen_tilgangsfeil() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(500);
        when(tilgangskontrollResponse.getStatusInfo()).thenReturn(TAU_I_PROPELLEN);

        brukerRessurs.hentBruker(FNR);

        verify(tilgangskontrollResponse).getStatus();
    }

    @Test
    public void bruker_har_tilgang() {
        Kontaktinfo kontaktinfo = new Kontaktinfo().tlf("12345678").skalHaVarsel(true);
        when(tilgangskontrollResponse.getStatus()).thenReturn(200);
        when(dkifService.hentKontaktinfoFnr(FNR)).thenReturn(kontaktinfo);
        when(brukerprofilService.hentBruker(FNR)).thenReturn(tpsPerson);

        RSBruker bruker = brukerRessurs.bruker(FNR);

        assertEquals(kontaktinfo.tlf, bruker.kontaktinfo.tlf);
        assertEquals(tpsPerson.navn, bruker.navn);

        verify(tilgangskontrollResponse, times(2)).getStatus();
        verify(brukerprofilService).hentBruker(FNR);
    }

    @Test(expected = ForbiddenException.class)
    public void bruker_har_ikke_tilgang() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(403);

        brukerRessurs.bruker(FNR);

        verify(tilgangskontrollResponse).getStatus();
    }

    @Test(expected = RuntimeException.class)
    public void bruker_annen_tilgangsfeil() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(500);
        when(tilgangskontrollResponse.getStatusInfo()).thenReturn(TAU_I_PROPELLEN);

        brukerRessurs.bruker(FNR);

        verify(tilgangskontrollResponse).getStatus();
    }

}

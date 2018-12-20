package no.nav.syfo.api.ressurser;

import no.nav.syfo.api.domain.RSHistorikk;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.MoteService;
import no.nav.syfo.service.HistorikkService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.ws.rs.ForbiddenException;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class HistorikkRessursTilgangTest extends AbstractRessursTilgangTest {

    @Mock
    private AktoerService aktoerService;
    @Mock
    private MoteService moteService;
    @Mock
    private HistorikkService historikkService;

    @InjectMocks
    private HistorikkRessurs historikkRessurs;

    @Test
    public void har_tilgang() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(200);
        when(aktoerService.hentAktoerIdForIdent(FNR)).thenReturn(AKTOER_ID);
        when(moteService.findMoterByBrukerAktoerId(AKTOER_ID)).thenReturn(emptyList());
        when(historikkService.opprettetHistorikk(emptyList())).thenReturn(emptyList());
        when(historikkService.flereTidspunktHistorikk(emptyList())).thenReturn(emptyList());
        when(historikkService.avbruttHistorikk(emptyList())).thenReturn(emptyList());
        when(historikkService.bekreftetHistorikk(emptyList())).thenReturn(emptyList());

        List<RSHistorikk> historikk = historikkRessurs.hentHistorikk(FNR);

        assertEquals(emptyList(), historikk);

        verify(tilgangskontrollResponse, times(2)).getStatus();
    }

    @Test(expected = ForbiddenException.class)
    public void har_ikke_tilgang() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(403);

        historikkRessurs.hentHistorikk(FNR);

        verify(tilgangskontrollResponse).getStatus();
    }

    @Test(expected = RuntimeException.class)
    public void annen_tilgangsfeil() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(500);
        when(tilgangskontrollResponse.getStatusInfo()).thenReturn(TAU_I_PROPELLEN);

        historikkRessurs.hentHistorikk(FNR);

        verify(tilgangskontrollResponse).getStatus();
    }

}

package no.nav.syfo.rest.api.resource;

import no.nav.syfo.rest.api.model.RSAktor;
import no.nav.syfo.service.AktoerService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.ws.rs.ForbiddenException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class AktoerRessursTilgangTest extends AbstractRessursTilgangTest {

    @Mock
    private AktoerService aktoerService;

    @InjectMocks
    private AktoerRessurs aktoerRessurs;

    @Test
    public void har_tilgang() {
        when(aktoerService.hentFnrForAktoer(AKTOER_ID)).thenReturn(FNR);
        when(tilgangskontrollResponse.getStatus()).thenReturn(200);

        RSAktor rsAktor = aktoerRessurs.get(AKTOER_ID);

        assertEquals(FNR, rsAktor.fnr);

        verify(aktoerService).hentFnrForAktoer(AKTOER_ID);
        verify(tilgangskontrollResponse, times(2)).getStatus();
    }

    @Test(expected = ForbiddenException.class)
    public void har_ikke_tilgang() {
        when(aktoerService.hentFnrForAktoer(AKTOER_ID)).thenReturn(FNR);
        when(tilgangskontrollResponse.getStatus()).thenReturn(403);

        aktoerRessurs.get(AKTOER_ID);

        verify(aktoerService).hentFnrForAktoer(AKTOER_ID);
        verify(tilgangskontrollResponse).getStatus();
    }

    @Test(expected = RuntimeException.class)
    public void annen_tilgangsfeil() {
        when(aktoerService.hentFnrForAktoer(AKTOER_ID)).thenReturn(FNR);
        when(tilgangskontrollResponse.getStatus()).thenReturn(500);
        when(tilgangskontrollResponse.getStatusInfo()).thenReturn(TAU_I_PROPELLEN);

        aktoerRessurs.get(AKTOER_ID);

        verify(aktoerService).hentFnrForAktoer(AKTOER_ID);
        verify(tilgangskontrollResponse).getStatus();
    }

}

package no.nav.syfo.rest.api.resource;

import no.nav.syfo.rest.api.model.RSBrukerPaaEnhet;
import no.nav.syfo.service.MotedeltakerService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.ws.rs.ForbiddenException;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class EnhetsOversiktRessursTilgangTest extends AbstractRessursTilgangTest {

    @Mock
    private MotedeltakerService motedeltakerService;

    @InjectMocks
    private EnhetRessurs enhetRessurs;

    private static final String ENHET_ID = "1234";

    @Test
    public void har_tilgang() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(200);
        when(motedeltakerService.sykmeldteMedMoteHvorBeggeHarSvart(ENHET_ID)).thenReturn(emptyList());

        List<RSBrukerPaaEnhet> brukerePaaEnhet = enhetRessurs.hentSykmeldteMedAktiveMoterForEnhet(ENHET_ID);

        assertEquals(emptyList(), brukerePaaEnhet);

        verify(tilgangskontrollResponse, times(2)).getStatus();
    }

    @Test(expected = ForbiddenException.class)
    public void har_ikke_tilgang() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(403);

        enhetRessurs.hentSykmeldteMedAktiveMoterForEnhet(ENHET_ID);

        verify(tilgangskontrollResponse).getStatus();
    }

    @Test(expected = RuntimeException.class)
    public void annen_tilgangsfeil() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(500);
        when(tilgangskontrollResponse.getStatusInfo()).thenReturn(TAU_I_PROPELLEN);

        enhetRessurs.hentSykmeldteMedAktiveMoterForEnhet(ENHET_ID);

        verify(tilgangskontrollResponse).getStatus();
    }


}

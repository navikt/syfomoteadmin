package no.nav.syfo.api.ressurser;

import no.nav.syfo.LocalApplication;
import no.nav.syfo.api.domain.RSBrukerPaaEnhet;
import no.nav.syfo.service.MotedeltakerService;
import no.nav.syfo.service.TilgangService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.ws.rs.ForbiddenException;
import java.util.List;

import static java.util.Collections.emptyList;
import static no.nav.syfo.testhelper.UserConstants.NAV_ENHET;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class EnhetsOversiktRessursTilgangTest {

    @Mock
    private MotedeltakerService motedeltakerService;
    @Mock
    private TilgangService tilgangService;
    @InjectMocks
    private EnhetRessurs enhetRessurs;

    @Test
    public void har_tilgang() {
        when(motedeltakerService.sykmeldteMedMoteHvorBeggeHarSvart(NAV_ENHET)).thenReturn(emptyList());

        List<RSBrukerPaaEnhet> brukerePaaEnhet = enhetRessurs.hentSykmeldteMedAktiveMoterForEnhet(NAV_ENHET);

        assertEquals(emptyList(), brukerePaaEnhet);
    }

    @Test(expected = ForbiddenException.class)
    public void har_ikke_tilgang() {
        doThrow(new ForbiddenException()).when(tilgangService).kastExceptionHvisIkkeVeilederHarTilgangTilEnhet(NAV_ENHET);

        enhetRessurs.hentSykmeldteMedAktiveMoterForEnhet(NAV_ENHET);
    }

    @Test(expected = RuntimeException.class)
    public void annen_tilgangsfeil() {
        doThrow(new RuntimeException()).when(tilgangService).kastExceptionHvisIkkeVeilederHarTilgangTilEnhet(NAV_ENHET);

        enhetRessurs.hentSykmeldteMedAktiveMoterForEnhet(NAV_ENHET);
    }
}

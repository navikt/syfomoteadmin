package no.nav.syfo.api.ressurser;

import no.nav.syfo.LocalApplication;
import no.nav.syfo.api.domain.RSHistorikk;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.HistorikkService;
import no.nav.syfo.service.MoteService;
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
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID;
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class HistorikkRessursTilgangTest {

    @Mock
    private AktoerService aktoerService;
    @Mock
    private HistorikkService historikkService;
    @Mock
    private MoteService moteService;
    @Mock
    private TilgangService tilgangService;
    @InjectMocks
    private HistorikkRessurs historikkRessurs;


    @Test
    public void har_tilgang() {
        when(aktoerService.hentAktoerIdForIdent(ARBEIDSTAKER_FNR)).thenReturn(ARBEIDSTAKER_AKTORID);
        when(moteService.findMoterByBrukerAktoerId(ARBEIDSTAKER_AKTORID)).thenReturn(emptyList());
        when(historikkService.opprettetHistorikk(emptyList())).thenReturn(emptyList());
        when(historikkService.flereTidspunktHistorikk(emptyList())).thenReturn(emptyList());
        when(historikkService.avbruttHistorikk(emptyList())).thenReturn(emptyList());
        when(historikkService.bekreftetHistorikk(emptyList())).thenReturn(emptyList());

        List<RSHistorikk> historikk = historikkRessurs.hentHistorikk(ARBEIDSTAKER_FNR);

        assertEquals(emptyList(), historikk);
    }

    @Test(expected = ForbiddenException.class)
    public void har_ikke_tilgang() {
        doThrow(new ForbiddenException()).when(tilgangService).kastExceptionHvisIkkeVeilederHarTilgangTilPerson(ARBEIDSTAKER_FNR);

        historikkRessurs.hentHistorikk(ARBEIDSTAKER_FNR);
    }

    @Test(expected = RuntimeException.class)
    public void annen_tilgangsfeil() {
        doThrow(new RuntimeException()).when(tilgangService).kastExceptionHvisIkkeVeilederHarTilgangTilPerson(ARBEIDSTAKER_FNR);

        historikkRessurs.hentHistorikk(ARBEIDSTAKER_FNR);
    }

}

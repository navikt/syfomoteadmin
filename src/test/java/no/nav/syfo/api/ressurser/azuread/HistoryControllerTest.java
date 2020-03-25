package no.nav.syfo.api.ressurser.azuread;

import no.nav.syfo.LocalApplication;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.api.domain.RSHistorikk;
import no.nav.syfo.api.ressurser.AbstractRessursTilgangTest;
import no.nav.syfo.service.HistorikkService;
import no.nav.syfo.service.MoteService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.text.ParseException;
import java.util.List;

import static java.util.Collections.emptyList;
import static no.nav.syfo.testhelper.OidcTestHelper.loggInnVeilederAzure;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class HistoryControllerTest extends AbstractRessursTilgangTest {

    @MockBean
    private AktorregisterConsumer aktorregisterConsumer;
    @MockBean
    private HistorikkService historikkService;
    @MockBean
    private MoteService moteService;

    @Inject
    private HistoryController historyController;

    @Before
    public void setup() throws ParseException {
        loggInnVeilederAzure(oidcRequestContextHolder, VEILEDER_ID);
    }

    @After
    public void tearDown() {
        loggUtAlle(oidcRequestContextHolder);
    }

    @Test
    public void getHistoryHasAccess() {
        when(moteService.findMoterByBrukerAktoerId(ARBEIDSTAKER_AKTORID)).thenReturn(emptyList());
        when(historikkService.opprettetHistorikk(emptyList())).thenReturn(emptyList());
        when(historikkService.flereTidspunktHistorikk(emptyList())).thenReturn(emptyList());
        when(historikkService.avbruttHistorikk(emptyList())).thenReturn(emptyList());
        when(historikkService.bekreftetHistorikk(emptyList())).thenReturn(emptyList());

        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.OK);

        List<RSHistorikk> history = historyController.getHistory(ARBEIDSTAKER_FNR);

        assertEquals(emptyList(), history);
    }

    @Test(expected = ForbiddenException.class)
    public void getHistoryNoAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN);

        historyController.getHistory(ARBEIDSTAKER_FNR);
    }

    @Test(expected = RuntimeException.class)
    public void getHistoryServerError() {
        loggUtAlle(oidcRequestContextHolder);

        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.INTERNAL_SERVER_ERROR);

        historyController.getHistory(ARBEIDSTAKER_FNR);
    }

}

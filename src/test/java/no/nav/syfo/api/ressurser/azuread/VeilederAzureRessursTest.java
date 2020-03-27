package no.nav.syfo.api.ressurser.azuread;

import no.nav.syfo.LocalApplication;
import no.nav.syfo.api.ressurser.AbstractRessursTilgangTest;
import no.nav.syfo.axsys.AxsysConsumer;
import no.nav.syfo.axsys.AxsysEnhet;
import no.nav.syfo.controller.internad.veileder.*;
import no.nav.syfo.domain.model.Veileder;
import no.nav.syfo.service.VeilederService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.text.ParseException;

import static java.util.Arrays.asList;
import static no.nav.syfo.config.mocks.NorgMock.NAV_ENHET_NAVN;
import static no.nav.syfo.testhelper.OidcTestHelper.loggInnVeilederAzure;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class VeilederAzureRessursTest extends AbstractRessursTilgangTest {

    @MockBean
    private AxsysConsumer axsysConsumer;

    @Inject
    private VeilederAzureRessurs veilederAzureRessurs;

    @MockBean
    private VeilederService veilederService;

    @Before
    public void setup() {
        when(axsysConsumer.enheter(VEILEDER_ID)).thenReturn(asList(
                new AxsysEnhet(
                        NAV_ENHET,
                        NAV_ENHET_NAVN
                )
        ));
        try {
            loggInnVeilederAzure(oidcRequestContextHolder, VEILEDER_ID);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Veileder veileder = new Veileder().navn(VEILEDER_NAVN);
        when(veilederService.hentVeileder(VEILEDER_ID)).thenReturn(veileder);
    }

    @Test
    public void hentInnloggetVeilederInfo() {
        RSVeilederInfo veilederInfo = veilederAzureRessurs.hentNavn();

        assertEquals(VEILEDER_NAVN, veilederInfo.getNavn());
        assertEquals(VEILEDER_ID, veilederInfo.getIdent());
    }

    @Test(expected = RuntimeException.class)
    public void finner_ikke_innlogget_bruker_veilederinfo() {
        loggUtAlle(oidcRequestContextHolder);

        veilederAzureRessurs.hentNavn();
    }

    @Test
    public void hentVeilederInfo() {
        RSVeilederInfo veilederInfo = veilederAzureRessurs.hentIdent(VEILEDER_ID);

        assertEquals(VEILEDER_NAVN, veilederInfo.getNavn());
        assertEquals(VEILEDER_ID, veilederInfo.getIdent());
    }

    @Test
    public void hentInnloggetVeilederEnheter() {
        RSEnheter rsEnheter = veilederAzureRessurs.hentEnheter();

        assertEquals(1, rsEnheter.getEnhetliste().size());
        assertEquals(NAV_ENHET_NAVN, rsEnheter.getEnhetliste().get(0).getNavn());
    }

    @Test(expected = RuntimeException.class)
    public void finner_ikke_innlogget_bruker_veilederenheter() {
        loggUtAlle(oidcRequestContextHolder);

        veilederAzureRessurs.hentEnheter();
    }
}

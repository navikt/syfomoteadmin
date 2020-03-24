package no.nav.syfo.api.ressurser.azuread;

import no.nav.syfo.LocalApplication;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.aktorregister.domain.AktorId;
import no.nav.syfo.api.domain.RSAktor;
import no.nav.syfo.api.ressurser.AbstractRessursTilgangTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.text.ParseException;

import static no.nav.syfo.testhelper.OidcTestHelper.loggInnVeilederAzure;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class AktorControllerTilgangTest extends AbstractRessursTilgangTest {

    @Inject
    private AktorController aktorController;

    @MockBean
    private AktorregisterConsumer aktorregisterConsumer;

    @Before
    public void setup() {
        when(aktorregisterConsumer.getFnrForAktorId(new AktorId(ARBEIDSTAKER_AKTORID))).thenReturn(ARBEIDSTAKER_FNR);
        try {
            loggInnVeilederAzure(oidcRequestContextHolder, VEILEDER_ID);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void hasAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.OK);

        RSAktor aktor = aktorController.get(ARBEIDSTAKER_AKTORID);

        assertEquals(ARBEIDSTAKER_FNR, aktor.fnr);
    }

    @Test(expected = ForbiddenException.class)
    public void noAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN);

        aktorController.get(ARBEIDSTAKER_AKTORID);
    }


    @Test(expected = RuntimeException.class)
    public void invalidUserContext() {
        loggUtAlle(oidcRequestContextHolder);

        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN);

        aktorController.get(ARBEIDSTAKER_AKTORID);
    }
}

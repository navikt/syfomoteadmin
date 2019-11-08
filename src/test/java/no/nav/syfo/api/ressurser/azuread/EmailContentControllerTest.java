package no.nav.syfo.api.ressurser.azuread;

import no.nav.syfo.LocalApplication;
import no.nav.syfo.api.domain.RSEpostInnhold;
import no.nav.syfo.api.ressurser.AbstractRessursTilgangTest;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.service.MoteService;
import no.nav.syfo.service.varselinnhold.ArbeidsgiverVarselService;
import no.nav.syfo.testhelper.MoteGenerator;
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
import java.util.UUID;

import static no.nav.syfo.testhelper.OidcTestHelper.loggInnVeilederAzure;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR;
import static no.nav.syfo.testhelper.UserConstants.VEILEDER_ID;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class EmailContentControllerTest extends AbstractRessursTilgangTest {

    @MockBean
    private MoteService moteService;

    @Inject
    private EmailContentController emailContentController;

    private MoteGenerator moteGenerator = new MoteGenerator();

    private UUID uuid = UUID.randomUUID();
    private Mote mote = moteGenerator.generateMote(uuid);

    @Before
    public void setup() {
        try {
            loggInnVeilederAzure(oidcRequestContextHolder, VEILEDER_ID);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        when(moteService.findMoteByMotedeltakerUuid(uuid.toString())).thenReturn(mote);
    }

    @Test
    public void getEmailContentHasAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.OK);

        RSEpostInnhold emailContent = emailContentController.getEmailContent(EmailContentController.BEKREFTET, uuid.toString(), "1");

        assertNotNull(emailContent.emne);
        assertNotNull(emailContent.innhold);
    }

    @Test(expected = ForbiddenException.class)
    public void getEmailContentNoAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN);

        emailContentController.getEmailContent(EmailContentController.BEKREFTET, uuid.toString(), null);
    }

    @Test(expected = RuntimeException.class)
    public void getEmailContentServerError() {
        loggUtAlle(oidcRequestContextHolder);

        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.INTERNAL_SERVER_ERROR);

        emailContentController.getEmailContent(EmailContentController.BEKREFTET, uuid.toString(), null);
    }
}

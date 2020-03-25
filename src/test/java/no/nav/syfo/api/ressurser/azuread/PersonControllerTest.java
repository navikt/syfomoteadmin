package no.nav.syfo.api.ressurser.azuread;

import no.nav.syfo.LocalApplication;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.aktorregister.domain.AktorId;
import no.nav.syfo.api.domain.RSBruker;
import no.nav.syfo.api.ressurser.AbstractRessursTilgangTest;
import no.nav.syfo.dkif.DigitalKontaktinfo;
import no.nav.syfo.dkif.DkifConsumer;
import no.nav.syfo.pdl.PdlConsumer;
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

import static no.nav.syfo.testhelper.DKIFKontakinformasjonResponseGeneratorKt.generateDigitalKontaktinfo;
import static no.nav.syfo.testhelper.OidcTestHelper.loggInnVeilederAzure;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class PersonControllerTest extends AbstractRessursTilgangTest {

    @MockBean
    private AktorregisterConsumer aktorregisterConsumer;
    @MockBean
    private DkifConsumer dkifConsumer;
    @MockBean
    private PdlConsumer pdlConsumer;

    @Inject
    private PersonController personController;

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
    public void getUserWithNameHasAccess() {
        when(pdlConsumer.fullName(ARBEIDSTAKER_FNR)).thenReturn(PERSON_NAVN);
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.OK);

        RSBruker user = personController.hentBruker(ARBEIDSTAKER_AKTORID);

        assertEquals(PERSON_NAVN, user.navn);
    }

    @Test(expected = ForbiddenException.class)
    public void getUserWithNameNoAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN);

        personController.hentBruker(ARBEIDSTAKER_AKTORID);
    }

    @Test(expected = RuntimeException.class)
    public void getUserWithNameServerError() {
        loggUtAlle(oidcRequestContextHolder);

        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.INTERNAL_SERVER_ERROR);

        personController.hentBruker(ARBEIDSTAKER_AKTORID);
    }

    @Test
    public void getUserHasAccess() {
        DigitalKontaktinfo digitalKontaktinfo = generateDigitalKontaktinfo();
        when(dkifConsumer.kontaktinformasjon(ARBEIDSTAKER_FNR)).thenReturn(digitalKontaktinfo);
        when(pdlConsumer.fullName(ARBEIDSTAKER_FNR)).thenReturn(PERSON_NAVN);

        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.OK);

        RSBruker user = personController.bruker(ARBEIDSTAKER_AKTORID);

        assertEquals(digitalKontaktinfo.getEpostadresse(), user.kontaktinfo.epost);
        assertEquals(digitalKontaktinfo.getMobiltelefonnummer(), user.kontaktinfo.tlf);
        assertEquals(digitalKontaktinfo.getKanVarsles(), user.kontaktinfo.reservasjon.skalHaVarsel);
        assertEquals(PERSON_NAVN, user.navn);
    }

    @Test(expected = ForbiddenException.class)
    public void getUserNoAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN);

        personController.bruker(ARBEIDSTAKER_AKTORID);
    }

    @Test(expected = RuntimeException.class)
    public void getUserServerError() {
        loggUtAlle(oidcRequestContextHolder);

        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.INTERNAL_SERVER_ERROR);

        personController.bruker(ARBEIDSTAKER_AKTORID);
    }
}

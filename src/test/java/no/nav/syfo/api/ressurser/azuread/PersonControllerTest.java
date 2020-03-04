package no.nav.syfo.api.ressurser.azuread;

import no.nav.syfo.LocalApplication;
import no.nav.syfo.api.domain.RSBruker;
import no.nav.syfo.api.ressurser.AbstractRessursTilgangTest;
import no.nav.syfo.domain.model.Kontaktinfo;
import no.nav.syfo.oidc.OIDCIssuer;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.service.DkifService;
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

import static no.nav.syfo.config.mocks.DkifMock.*;
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
    private DkifService dkifService;
    @MockBean
    private PdlConsumer pdlConsumer;

    @Inject
    private PersonController personController;

    @Before
    public void setup() {
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
        Kontaktinfo kontaktinfo = new Kontaktinfo()
                .tlf(PERSON_TLF)
                .epost(PERSON_EMAIL)
                .skalHaVarsel(Boolean.valueOf(PERSON_RESERVASJON))
                .feilAarsak(Kontaktinfo.FeilAarsak.RESERVERT);
        when(dkifService.hentKontaktinfoFnr(ARBEIDSTAKER_FNR, OIDCIssuer.AZURE)).thenReturn(kontaktinfo);
        when(pdlConsumer.fullName(ARBEIDSTAKER_FNR)).thenReturn(PERSON_NAVN);

        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.OK);

        RSBruker user = personController.bruker(ARBEIDSTAKER_AKTORID);

        assertEquals(PERSON_EMAIL, user.kontaktinfo.epost);
        assertEquals(PERSON_TLF, user.kontaktinfo.tlf);
        assertEquals(false, user.kontaktinfo.reservasjon.skalHaVarsel);
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

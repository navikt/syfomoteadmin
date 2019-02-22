package no.nav.syfo.api.ressurser;

import no.nav.syfo.api.domain.RSAktor;
import no.nav.syfo.service.AktoerService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import static no.nav.syfo.testhelper.OidcTestHelper.loggInnVeileder;
import static no.nav.syfo.testhelper.UserConstants.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

public class AktoerRessursTilgangTest extends AbstractRessursTilgangTest {

    @Inject
    private AktoerRessurs aktoerRessurs;
    @Mock
    private AktoerService aktoerService;

    @Before
    public void setup() {
        loggInnVeileder(oidcRequestContextHolder, VEILEDER_ID);
        when(aktoerService.hentFnrForAktoer(ARBEIDSTAKER_AKTORID)).thenReturn(ARBEIDSTAKER_FNR);
    }

    @Test
    public void har_tilgang() {
        mockSvarFraTilgangTilBruker(ARBEIDSTAKER_FNR, OK);

        RSAktor rsAktor = aktoerRessurs.get(ARBEIDSTAKER_AKTORID);

        assertEquals(ARBEIDSTAKER_FNR, rsAktor.fnr);
    }

    @Test(expected = ForbiddenException.class)
    public void har_ikke_tilgang() {
        mockSvarFraTilgangTilBruker(ARBEIDSTAKER_FNR, FORBIDDEN);

        aktoerRessurs.get(ARBEIDSTAKER_AKTORID);
    }

    @Test(expected = RuntimeException.class)
    public void annen_tilgangsfeil() {
        mockSvarFraTilgangTilBruker(ARBEIDSTAKER_FNR, INTERNAL_SERVER_ERROR);

        aktoerRessurs.get(ARBEIDSTAKER_AKTORID);
    }
}

package no.nav.syfo.api.ressurser.azuread;

import no.nav.syfo.LocalApplication;
import no.nav.syfo.api.domain.RSVirksomhet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import static no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER;
import static no.nav.syfo.testhelper.UserConstants.VIRKSOMHET_NAME;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class VirksomhetControllerTest {

    @Inject
    private VirksomhetController virksomhetController;

    @Test
    public void getUserWithNameHasAccess() {
        RSVirksomhet virksomhet = virksomhetController.getVirksomhetsnavn(VIRKSOMHETSNUMMER);

        assertEquals(VIRKSOMHET_NAME, virksomhet.navn);
    }
}

package no.nav.syfo.api.ressurser.azuread.actions;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.LocalApplication;
import no.nav.syfo.api.domain.RSOverforMoter;
import no.nav.syfo.service.MoteService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static no.nav.syfo.testhelper.OidcTestHelper.loggInnVeilederAzure;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.VEILEDER_ID;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class MoterActionsControllerTest {

    @MockBean
    private MoteService moteService;

    @Inject
    public OIDCRequestContextHolder oidcRequestContextHolder;

    @Inject
    private MoterActionsController moterActionsController;

    @Before
    public void setup() throws ParseException {
        loggInnVeilederAzure(oidcRequestContextHolder, VEILEDER_ID);
    }

    @Test
    public void transferDialogmoterHasAccess() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        RSOverforMoter rsOverforMoter = getRSOverforMote(asList(
                uuid1.toString(),
                uuid2.toString()
        ));

        moterActionsController.transferDialogmoter(rsOverforMoter);

        verify(moteService, times(1)).overforMoteTil(uuid1.toString(), VEILEDER_ID);
        verify(moteService, times(1)).overforMoteTil(uuid2.toString(), VEILEDER_ID);
    }

    @Test(expected = RuntimeException.class)
    public void transferDialogmoterServerError() {
        loggUtAlle(oidcRequestContextHolder);

        RSOverforMoter rsOverforMoter = getRSOverforMote(asList(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        ));

        moterActionsController.transferDialogmoter(rsOverforMoter);
    }

    private RSOverforMoter getRSOverforMote(List<String> uuidList) {
        RSOverforMoter rsOverforMoter = new RSOverforMoter();
        rsOverforMoter.moteUuidListe = uuidList;
        return rsOverforMoter;
    }
}

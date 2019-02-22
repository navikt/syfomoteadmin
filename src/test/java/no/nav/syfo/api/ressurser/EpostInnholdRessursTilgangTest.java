package no.nav.syfo.api.ressurser;

import no.nav.syfo.api.domain.RSEpostInnhold;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.Motedeltaker;
import no.nav.syfo.domain.model.MotedeltakerAktorId;
import no.nav.syfo.domain.model.TidOgSted;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.MoteService;
import no.nav.syfo.service.TilgangService;
import no.nav.syfo.service.varselinnhold.ArbeidsgiverVarselService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.ws.rs.ForbiddenException;
import java.time.LocalDateTime;

import static java.util.Collections.singletonList;
import static no.nav.syfo.api.ressurser.EpostInnholdRessurs.BEKREFTET;
import static no.nav.syfo.api.ressurser.EpostInnholdRessurs.BRUKER;
import static no.nav.syfo.testhelper.OidcTestHelper.loggInnVeileder;
import static no.nav.syfo.testhelper.UserConstants.*;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class EpostInnholdRessursTilgangTest extends AbstractRessursTilgangTest {

    private static final String UUID = "abcd1234-abcd-1234-5678-abcdef123456";

    @Mock
    private AktoerService aktoerService;
    @Mock
    private MoteService moteService;
    @Mock
    private TilgangService tilgangService;
    @Mock
    private ArbeidsgiverVarselService arbeidsgiverVarselService;
    @InjectMocks
    private EpostInnholdRessurs epostInnholdRessurs;

    private final TidOgSted tidOgSted = new TidOgSted()
            .id(1L)
            .tid(LocalDateTime.now().plusMonths(1L))
            .sted("Nav");
    private final Motedeltaker moteDeltaker = new MotedeltakerAktorId()
            .uuid(UUID)
            .aktorId(ARBEIDSTAKER_AKTORID)
            .motedeltakertype(BRUKER)
            .tidOgStedAlternativer(singletonList(tidOgSted));
    private final Mote Mote = new Mote()
            .motedeltakere(singletonList(moteDeltaker));

    @Before
    public void setup() {
        loggInnVeileder(oidcRequestContextHolder, VEILEDER_ID);
        when(aktoerService.hentFnrForAktoer(ARBEIDSTAKER_AKTORID)).thenReturn(ARBEIDSTAKER_FNR);
        when(moteService.findMoteByMotedeltakerUuid(UUID)).thenReturn(Mote);
    }

    @Test
    public void har_tilgang() {
        RSEpostInnhold rsEpostInnhold = epostInnholdRessurs.genererEpostInnholdForFrontend(BEKREFTET, UUID, "1");

        assertNotNull(rsEpostInnhold.emne);
        assertNotNull(rsEpostInnhold.innhold);

        verify(moteService).findMoteByMotedeltakerUuid(UUID);
    }

    @Test(expected = ForbiddenException.class)
    public void har_ikke_tilgang() {
        doThrow(new ForbiddenException()).when(tilgangService).kastExceptionHvisIkkeVeilederHarTilgangTilPerson(ARBEIDSTAKER_FNR);

        epostInnholdRessurs.genererEpostInnholdForFrontend(BEKREFTET, UUID, null);
    }

    @Test(expected = RuntimeException.class)
    public void annen_tilgangsfeil() {
        doThrow(new RuntimeException()).when(tilgangService).kastExceptionHvisIkkeVeilederHarTilgangTilPerson(ARBEIDSTAKER_FNR);

        epostInnholdRessurs.genererEpostInnholdForFrontend(BEKREFTET, UUID, null);
    }
}

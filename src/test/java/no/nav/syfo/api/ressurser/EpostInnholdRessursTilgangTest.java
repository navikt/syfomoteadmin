package no.nav.syfo.api.ressurser;

import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.Motedeltaker;
import no.nav.syfo.domain.model.MotedeltakerAktorId;
import no.nav.syfo.domain.model.TidOgSted;
import no.nav.syfo.api.domain.RSEpostInnhold;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.MoteService;
import no.nav.syfo.service.varselinnhold.ArbeidsgiverVarselService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.ws.rs.ForbiddenException;
import java.time.LocalDateTime;

import static java.util.Collections.singletonList;
import static no.nav.syfo.api.ressurser.EpostInnholdRessurs.BEKREFTET;
import static no.nav.syfo.api.ressurser.EpostInnholdRessurs.BRUKER;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class EpostInnholdRessursTilgangTest extends AbstractRessursTilgangTest {

    private static final String UUID = "abcd1234-abcd-1234-5678-abcdef123456";
    private static final String AKTOR_ID = "123456789";

    @Mock
    private MoteService moteService;
    @Mock
    private AktoerService aktoerService;
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
            .aktorId(AKTOR_ID)
            .motedeltakertype(BRUKER)
            .tidOgStedAlternativer(singletonList(tidOgSted));
    private final Mote Mote = new Mote()
            .motedeltakere(singletonList(moteDeltaker));

    @Test
    public void har_tilgang() {
        when(moteService.findMoteByMotedeltakerUuid(UUID)).thenReturn(Mote);
        when(aktoerService.hentFnrForAktoer(AKTOR_ID)).thenReturn(FNR);
        when(tilgangskontrollResponse.getStatus()).thenReturn(200);

        RSEpostInnhold rsEpostInnhold = epostInnholdRessurs.genererEpostInnholdForFrontend(BEKREFTET, UUID, "1");

        assertNotNull(rsEpostInnhold.emne);
        assertNotNull(rsEpostInnhold.innhold);

        verify(tilgangskontrollResponse, times(2)).getStatus();
        verify(moteService).findMoteByMotedeltakerUuid(UUID);
    }

    @Test(expected = ForbiddenException.class)
    public void har_ikke_tilgang() {
        when(moteService.findMoteByMotedeltakerUuid(UUID)).thenReturn(Mote);
        when(aktoerService.hentFnrForAktoer(AKTOR_ID)).thenReturn(FNR);
        when(tilgangskontrollResponse.getStatus()).thenReturn(403);

        epostInnholdRessurs.genererEpostInnholdForFrontend(BEKREFTET, UUID, null);

        verify(tilgangskontrollResponse).getStatus();
    }

    @Test(expected = RuntimeException.class)
    public void annen_tilgangsfeil() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(500);
        when(tilgangskontrollResponse.getStatusInfo()).thenReturn(TAU_I_PROPELLEN);

        epostInnholdRessurs.genererEpostInnholdForFrontend(BEKREFTET, UUID, null);

        verify(tilgangskontrollResponse).getStatus();
    }


}

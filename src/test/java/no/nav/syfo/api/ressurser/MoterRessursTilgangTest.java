package no.nav.syfo.api.ressurser;

import no.nav.syfo.api.domain.RSMote;
import no.nav.syfo.api.domain.nyttmoterequest.RSNyttMoteRequest;
import no.nav.syfo.domain.model.*;
import no.nav.syfo.repository.dao.MotedeltakerDAO;
import no.nav.syfo.repository.dao.TidOgStedDAO;
import no.nav.syfo.repository.model.PMotedeltakerAktorId;
import no.nav.syfo.repository.model.PMotedeltakerArbeidsgiver;
import no.nav.syfo.service.*;
import no.nav.syfo.service.varselinnhold.ArbeidsgiverVarselService;
import no.nav.syfo.service.varselinnhold.SykmeldtVarselService;
import no.nav.syfo.service.varselinnhold.VeilederVarselService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.ws.rs.ForbiddenException;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

public class MoterRessursTilgangTest extends AbstractRessursTilgangTest {

    private static final String AKTOER_ID_2 = "1101010101010";
    private static final String FNR_2 = "11010101010";
    private static final String NAVENHET = "1234";
    private static final TpsPerson skjermet_tpsPerson = new TpsPerson().skjermetBruker(true);
    private static final TpsPerson tpsPerson = new TpsPerson().skjermetBruker(false);

    @Mock
    private AktoerService aktoerService;
    @Mock
    private MoteService moteService;
    @Mock
    private TidOgStedDAO tidOgStedDAO;
    @Mock
    private HendelseService hendelseService;
    @Mock
    private MotedeltakerDAO motedeltakerDAO;
    @Mock
    private NorgService norgService;
    @Mock
    private BrukerprofilService brukerprofilService;
    @Mock
    private VeilederService veilederService;
    @Mock
    private VeilederVarselService veilederVarselService;
    @Mock
    private ArbeidsgiverVarselService arbeidsgiverVarselService;
    @Mock
    private SykefravaersoppfoelgingService sykefravaersoppfoelgingService;
    @Mock
    private SykmeldtVarselService sykmeldtVarselService;

    @InjectMocks
    private MoterRessurs moterRessurs;

    private List<Mote> MoteList = asList(
            new Mote()
                    .id(1337L)
                    .status(MoteStatus.OPPRETTET)
                    .opprettetTidspunkt(now().minusMonths(1L))
                    .motedeltakere(singletonList(new MotedeltakerAktorId().aktorId(AKTOER_ID))),
            new Mote()
                    .id(1338L)
                    .status(MoteStatus.OPPRETTET)
                    .opprettetTidspunkt(now().minusMonths(2L))
                    .motedeltakere(singletonList(new MotedeltakerAktorId().aktorId(AKTOER_ID_2)))
    );

    @Test
    public void hentMoter_fnr_veileder_har_tilgang() {
        Mote Mote1 = new Mote()
                .id(1337L)
                .status(MoteStatus.OPPRETTET)
                .opprettetTidspunkt(now().minusMonths(1L))
                .motedeltakere(singletonList(new MotedeltakerAktorId().aktorId(AKTOER_ID)));
        Mote Mote2 = new Mote()
                .id(1338L)
                .status(MoteStatus.OPPRETTET)
                .opprettetTidspunkt(now().minusMonths(2L))
                .motedeltakere(singletonList(new MotedeltakerAktorId().aktorId(AKTOER_ID)));

        when(tilgangskontrollResponse.getStatus()).thenReturn(200);
        when(aktoerService.hentFnrForAktoer(AKTOER_ID)).thenReturn(FNR);
        when(brukerprofilService.hentBruker(FNR)).thenReturn(tpsPerson);

        when(aktoerService.hentAktoerIdForIdent(FNR)).thenReturn(AKTOER_ID);
        when(moteService.findMoterByBrukerAktoerId(AKTOER_ID)).thenReturn(asList(Mote1, Mote2));
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterRessurs.hentMoter(null, FNR, false, null, false);

        assertEquals(AKTOER_ID, moteList.get(0).aktorId);
        assertEquals(AKTOER_ID, moteList.get(1).aktorId);

        verify(brukerprofilService, times(3)).hentBruker(FNR);
        verify(tilgangskontrollResponse, times(6)).getStatus();
        verify(aktoerService, times(4)).hentFnrForAktoer(AKTOER_ID);
        verify(aktoerService, times(1)).hentAktoerIdForIdent(FNR);
        verify(moteService).findMoterByBrukerAktoerId(AKTOER_ID);
    }

    @Test(expected = ForbiddenException.class)
    public void hentMoter_fnr_veileder_har_ikke_tilgang_pga_rolle() {
        when(brukerprofilService.hentBruker(FNR)).thenReturn(tpsPerson);
        when(tilgangskontrollResponse.getStatus())
                .thenReturn(403)
                .thenReturn(403);

        moterRessurs.hentMoter(null, FNR, false, null, false);

        verify(brukerprofilService).hentBruker(FNR);
        verify(tilgangskontrollResponse).getStatus();
    }

    @Test(expected = ForbiddenException.class)
    public void hentMoter_fnr_veileder_har_ikke_tilgang_pga_skjermet_bruker() {
        when(brukerprofilService.hentBruker(FNR)).thenReturn(skjermet_tpsPerson);
        when(tilgangskontrollResponse.getStatus()).thenReturn(200);

        moterRessurs.hentMoter(null, FNR, false, null, false);

        verify(tilgangskontrollResponse).getStatus();
    }

    @Test(expected = RuntimeException.class)
    public void hentMoter_fnr_veileder_annen_tilgangsfeil() {
        when(brukerprofilService.hentBruker(FNR)).thenReturn(tpsPerson);
        when(tilgangskontrollResponse.getStatus()).thenReturn(500);
        when(tilgangskontrollResponse.getStatusInfo()).thenReturn(TAU_I_PROPELLEN);

        moterRessurs.hentMoter(null, FNR, false, null, false);

        verify(brukerprofilService).hentBruker(FNR);
        verify(tilgangskontrollResponse).getStatus();
    }

    @Test
    public void hentMoter_navenhet_veileder_har_full_tilgang() {
        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(moteService.findMoterByBrukerNavEnhet(NAVENHET)).thenReturn(MoteList);
        when(brukerprofilService.hentBruker(FNR))
                .thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2))
                .thenReturn(tpsPerson);
        when(tilgangskontrollResponse.getStatus()).thenReturn(200);
        when(aktoerService.hentFnrForAktoer(AKTOER_ID)).thenReturn(FNR);
        when(aktoerService.hentFnrForAktoer(AKTOER_ID_2)).thenReturn(FNR_2);
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterRessurs.hentMoter(null, null, false, NAVENHET, false);

        assertEquals(2, moteList.size());
        assertEquals(AKTOER_ID, moteList.get(0).aktorId);
        assertEquals(AKTOER_ID_2, moteList.get(1).aktorId);

        verify(brukerprofilService).hentBruker(FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
        verify(tilgangskontrollResponse, times(4)).getStatus();
    }

    @Test
    public void hentMoter_navenhet_veileder_har_delvis_tilgang_pga_rolle() {
        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(moteService.findMoterByBrukerNavEnhet(NAVENHET)).thenReturn(MoteList);
        when(brukerprofilService.hentBruker(FNR))
                .thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2))
                .thenReturn(tpsPerson);
        when(tilgangskontrollResponse.getStatus())
                .thenReturn(200)
                .thenReturn(200)
                .thenReturn(403)
                .thenReturn(403);
        when(aktoerService.hentFnrForAktoer(AKTOER_ID)).thenReturn(FNR);
        when(aktoerService.hentFnrForAktoer(AKTOER_ID_2)).thenReturn(FNR_2);
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterRessurs.hentMoter(null, null, false, NAVENHET, false);

        assertEquals(1, moteList.size());
        assertEquals(AKTOER_ID, moteList.get(0).aktorId);

        verify(brukerprofilService).hentBruker(FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
        verify(tilgangskontrollResponse, times(4)).getStatus();
    }

    @Test
    public void hentMoter_navenhet_veileder_har_delvis_tilgang_pga_skjermet_bruker() {
        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(moteService.findMoterByBrukerNavEnhet(NAVENHET)).thenReturn(MoteList);
        when(brukerprofilService.hentBruker(FNR))
                .thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2))
                .thenReturn(skjermet_tpsPerson);
        when(tilgangskontrollResponse.getStatus()).thenReturn(200);
        when(aktoerService.hentFnrForAktoer(AKTOER_ID)).thenReturn(FNR);
        when(aktoerService.hentFnrForAktoer(AKTOER_ID_2)).thenReturn(FNR_2);
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterRessurs.hentMoter(null, null, false, NAVENHET, false);

        assertEquals(1, moteList.size());
        assertEquals(AKTOER_ID, moteList.get(0).aktorId);

        verify(brukerprofilService).hentBruker(FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test
    public void hentMoter_navenhet_veileder_har_ikke_tilgang_pga_rolle() {
        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(moteService.findMoterByBrukerNavEnhet(NAVENHET)).thenReturn(MoteList);
        when(brukerprofilService.hentBruker(FNR)).thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2)).thenReturn(tpsPerson);
        when(tilgangskontrollResponse.getStatus()).thenReturn(403);
        when(aktoerService.hentFnrForAktoer(AKTOER_ID)).thenReturn(FNR);
        when(aktoerService.hentFnrForAktoer(AKTOER_ID_2)).thenReturn(FNR_2);
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterRessurs.hentMoter(null, null, false, NAVENHET, false);

        assertEquals(0, moteList.size());

        verify(brukerprofilService).hentBruker(FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
        verify(tilgangskontrollResponse, times(4)).getStatus();
    }

    @Test
    public void hentMoter_navenhet_veileder_har_ikke_tilgang_pga_skjerming() {
        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(moteService.findMoterByBrukerNavEnhet(NAVENHET)).thenReturn(MoteList);
        when(brukerprofilService.hentBruker(FNR)).thenReturn(skjermet_tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2)).thenReturn(skjermet_tpsPerson);
        when(tilgangskontrollResponse.getStatus()).thenReturn(200);
        when(aktoerService.hentFnrForAktoer(AKTOER_ID)).thenReturn(FNR);
        when(aktoerService.hentFnrForAktoer(AKTOER_ID_2)).thenReturn(FNR_2);
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterRessurs.hentMoter(null, null, false, NAVENHET, false);

        assertEquals(0, moteList.size());

        verify(brukerprofilService).hentBruker(FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test(expected = RuntimeException.class)
    public void hentMoter_navenhet_annen_tilgangsfeil() {
        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(moteService.findMoterByBrukerNavEnhet(NAVENHET)).thenReturn(MoteList);
        when(aktoerService.hentFnrForAktoer(AKTOER_ID)).thenReturn(FNR);
        when(aktoerService.hentFnrForAktoer(AKTOER_ID_2)).thenReturn(FNR_2);
        when(brukerprofilService.hentBruker(FNR)).thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2)).thenReturn(tpsPerson);
        when(tilgangskontrollResponse.getStatus())
                .thenReturn(200)
                .thenReturn(500);
        when(tilgangskontrollResponse.getStatusInfo()).thenReturn(TAU_I_PROPELLEN);
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        moterRessurs.hentMoter(null, null, false, NAVENHET, false);

        verify(brukerprofilService).hentBruker(FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
        verify(tilgangskontrollResponse).getStatus();
    }

    @Test
    public void opprettMoter_har_tilgang() {
        final RSNyttMoteRequest nyttMoteRequest = new RSNyttMoteRequest()
                .fnr(FNR)
                .orgnummer("123");

        when(brukerprofilService.hentBruker(FNR)).thenReturn(tpsPerson);
        when(tilgangskontrollResponse.getStatus()).thenReturn(200);
        when(aktoerService.hentAktoerIdForIdent(FNR)).thenReturn(AKTOER_ID);
        when(sykefravaersoppfoelgingService.hentNaermesteLeder(AKTOER_ID, nyttMoteRequest.orgnummer)).thenReturn(
                new NaermesteLeder()
                        .navn("Frida Frisk")
                        .epost("frida.frisk@bedrift.no")
        );
        when(moteService.opprettMote(any())).thenReturn(new Mote().id(1L));
        when(tidOgStedDAO.create(any())).thenReturn(new TidOgSted());
        when(motedeltakerDAO.create(any(PMotedeltakerAktorId.class))).thenReturn(new MotedeltakerAktorId());
        when(motedeltakerDAO.create(any(PMotedeltakerArbeidsgiver.class))).thenReturn(new MotedeltakerArbeidsgiver());
        when(veilederService.hentVeileder(any())).thenReturn(new Veileder());

        moterRessurs.opprett(nyttMoteRequest);

        verify(brukerprofilService).hentBruker(FNR);
        verify(tilgangskontrollResponse, times(2)).getStatus();
    }

    @Test(expected = ForbiddenException.class)
    public void opprettMoter_ikke_tilgang_pga_skjermet_bruker() {
        when(brukerprofilService.hentBruker(FNR)).thenReturn(skjermet_tpsPerson);
        when(tilgangskontrollResponse.getStatus()).thenReturn(200);

        moterRessurs.opprett(new RSNyttMoteRequest().fnr(FNR));

        verify(brukerprofilService).hentBruker(FNR);
    }

    @Test(expected = ForbiddenException.class)
    public void opprettMoter_ikke_tilgang_pga_rolle() {
        when(brukerprofilService.hentBruker(FNR)).thenReturn(tpsPerson);
        when(tilgangskontrollResponse.getStatus()).thenReturn(403);

        moterRessurs.opprett(new RSNyttMoteRequest().fnr(FNR));

        verify(brukerprofilService).hentBruker(FNR);
        verify(tilgangskontrollResponse).getStatus();
    }

    @Test(expected = RuntimeException.class)
    public void opprettMoter_annen_tilgangsfeil() {
        when(brukerprofilService.hentBruker(FNR)).thenReturn(tpsPerson);
        when(tilgangskontrollResponse.getStatus()).thenReturn(500);
        when(tilgangskontrollResponse.getStatusInfo()).thenReturn(TAU_I_PROPELLEN);

        moterRessurs.opprett(new RSNyttMoteRequest().fnr(FNR));

        verify(brukerprofilService).hentBruker(FNR);
        verify(tilgangskontrollResponse).getStatus();
    }

}

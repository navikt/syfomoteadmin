package no.nav.syfo.api.ressurser.azuread;

import no.nav.syfo.LocalApplication;
import no.nav.syfo.api.domain.RSMote;
import no.nav.syfo.api.domain.nyttmoterequest.RSNyttMoteRequest;
import no.nav.syfo.api.ressurser.AbstractRessursTilgangTest;
import no.nav.syfo.domain.model.*;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.dao.MotedeltakerDAO;
import no.nav.syfo.repository.dao.TidOgStedDAO;
import no.nav.syfo.repository.model.PMotedeltakerAktorId;
import no.nav.syfo.repository.model.PMotedeltakerArbeidsgiver;
import no.nav.syfo.service.*;
import no.nav.syfo.service.varselinnhold.ArbeidsgiverVarselService;
import no.nav.syfo.service.varselinnhold.SykmeldtVarselService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.text.ParseException;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static no.nav.syfo.testhelper.OidcTestHelper.loggInnVeilederAzure;
import static no.nav.syfo.testhelper.UserConstants.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class MoterInternControllerTest extends AbstractRessursTilgangTest {

    private static final String AKTOER_ID_2 = "1101010101010";
    private static final String FNR_2 = "11010101010";
    private static final TpsPerson skjermet_tpsPerson = new TpsPerson().skjermetBruker(true);
    private static final TpsPerson tpsPerson = new TpsPerson().skjermetBruker(false);

    @MockBean
    private AktoerService aktoerService;
    @MockBean
    private MoteService moteService;
    @MockBean
    private EnhetService enhetService;
    @MockBean
    private Metrikk metrikk;
    @MockBean
    private TidOgStedDAO tidOgStedDAO;
    @MockBean
    private HendelseService hendelseService;
    @MockBean
    private MotedeltakerDAO motedeltakerDAO;
    @MockBean
    private NorgService norgService;
    @MockBean
    private BrukerprofilService brukerprofilService;
    @MockBean
    private VeilederService veilederService;
    @MockBean
    private ArbeidsgiverVarselService arbeidsgiverVarselService;
    @MockBean
    private SykefravaersoppfoelgingService sykefravaersoppfoelgingService;
    @MockBean
    private SykmeldtVarselService sykmeldtVarselService;
    @MockBean
    private TilgangService tilgangService;

    @Inject
    private MoterInternController moterController;

    @Before
    public void setup() throws ParseException {
        loggInnVeilederAzure(oidcRequestContextHolder, VEILEDER_ID);

        when(aktoerService.hentFnrForAktoer(ARBEIDSTAKER_AKTORID)).thenReturn(ARBEIDSTAKER_FNR);
        when(aktoerService.hentAktoerIdForIdent(ARBEIDSTAKER_FNR)).thenReturn(ARBEIDSTAKER_AKTORID);
        when(aktoerService.hentFnrForAktoer(AKTOER_ID_2)).thenReturn(FNR_2);
        when(moteService.findMoterByBrukerNavEnhet(NAV_ENHET)).thenReturn(MoteList);
        when(enhetService.finnArbeidstakersBehandlendeEnhet(any())).thenReturn(NAV_ENHET);
    }

    private List<Mote> MoteList = asList(
            new Mote()
                    .id(1337L)
                    .status(MoteStatus.OPPRETTET)
                    .opprettetTidspunkt(now().minusMonths(1L))
                    .motedeltakere(singletonList(new MotedeltakerAktorId().aktorId(ARBEIDSTAKER_AKTORID))),
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
                .motedeltakere(singletonList(new MotedeltakerAktorId().aktorId(ARBEIDSTAKER_AKTORID)));
        Mote Mote2 = new Mote()
                .id(1338L)
                .status(MoteStatus.OPPRETTET)
                .opprettetTidspunkt(now().minusMonths(2L))
                .motedeltakere(singletonList(new MotedeltakerAktorId().aktorId(ARBEIDSTAKER_AKTORID)));

        when(tilgangService.harVeilederTilgangTilPersonViaAzure(ARBEIDSTAKER_FNR)).thenReturn(true);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);
        when(moteService.findMoterByBrukerAktoerId(ARBEIDSTAKER_AKTORID)).thenReturn(asList(Mote1, Mote2));
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterController.hentMoter(null, ARBEIDSTAKER_FNR, false, null, false);

        assertEquals(ARBEIDSTAKER_AKTORID, moteList.get(0).aktorId);
        assertEquals(ARBEIDSTAKER_AKTORID, moteList.get(1).aktorId);

        verify(brukerprofilService, times(3)).hentBruker(ARBEIDSTAKER_FNR);
        verify(aktoerService, times(4)).hentFnrForAktoer(ARBEIDSTAKER_AKTORID);
        verify(aktoerService, times(1)).hentAktoerIdForIdent(ARBEIDSTAKER_FNR);
        verify(moteService).findMoterByBrukerAktoerId(ARBEIDSTAKER_AKTORID);
    }

    @Test(expected = ForbiddenException.class)
    public void hentMoter_fnr_veileder_har_ikke_tilgang_pga_rolle() {
        when(tilgangService.harVeilederTilgangTilPersonViaAzure(ARBEIDSTAKER_FNR)).thenReturn(false);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);

        moterController.hentMoter(null, ARBEIDSTAKER_FNR, false, null, false);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
    }

    @Test(expected = ForbiddenException.class)
    public void hentMoter_fnr_veileder_har_ikke_tilgang_pga_skjermet_bruker() {
        when(tilgangService.harVeilederTilgangTilPersonViaAzure(ARBEIDSTAKER_FNR)).thenReturn(true);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(skjermet_tpsPerson);

        moterController.hentMoter(null, ARBEIDSTAKER_FNR, false, null, false);
    }

    @Test(expected = RuntimeException.class)
    public void hentMoter_fnr_veileder_annen_tilgangsfeil() {
        when(tilgangService.harVeilederTilgangTilPersonViaAzure(ARBEIDSTAKER_FNR)).thenReturn(false);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);

        moterController.hentMoter(null, ARBEIDSTAKER_FNR, false, null, false);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
    }

    @Test
    public void hentMoter_navenhet_veileder_har_full_tilgang() {
        when(tilgangService.harVeilederTilgangTilPersonViaAzure(ARBEIDSTAKER_FNR)).thenReturn(true);
        when(tilgangService.harVeilederTilgangTilPersonViaAzure(FNR_2)).thenReturn(true);

        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR))
                .thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2))
                .thenReturn(tpsPerson);
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterController.hentMoter(null, null, false, NAV_ENHET, false);

        assertEquals(2, moteList.size());
        assertEquals(ARBEIDSTAKER_AKTORID, moteList.get(0).aktorId);
        assertEquals(AKTOER_ID_2, moteList.get(1).aktorId);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test
    public void hentMoter_navenhet_veileder_har_delvis_tilgang_pga_rolle() {
        when(tilgangService.harVeilederTilgangTilPersonViaAzure(any()))
                .thenReturn(true)
                .thenReturn(false);

        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR))
                .thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2))
                .thenReturn(tpsPerson);
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterController.hentMoter(null, null, false, NAV_ENHET, false);

        assertEquals(1, moteList.size());
        assertEquals(ARBEIDSTAKER_AKTORID, moteList.get(0).aktorId);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test
    public void hentMoter_navenhet_veileder_har_delvis_tilgang_pga_skjermet_bruker() {
        when(tilgangService.harVeilederTilgangTilPersonViaAzure(ARBEIDSTAKER_FNR)).thenReturn(true);

        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR))
                .thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2))
                .thenReturn(skjermet_tpsPerson);

        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterController.hentMoter(null, null, false, NAV_ENHET, false);

        assertEquals(1, moteList.size());
        assertEquals(ARBEIDSTAKER_AKTORID, moteList.get(0).aktorId);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test
    public void hentMoter_navenhet_veileder_har_ikke_tilgang_pga_rolle() {
        when(tilgangService.harVeilederTilgangTilPersonViaAzure(ARBEIDSTAKER_FNR)).thenReturn(false);

        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2)).thenReturn(tpsPerson);

        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterController.hentMoter(null, null, false, NAV_ENHET, false);

        assertEquals(0, moteList.size());

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test
    public void hentMoter_navenhet_veileder_har_ikke_tilgang_pga_skjerming() {
        when(tilgangService.harVeilederTilgangTilPersonViaAzure(ARBEIDSTAKER_FNR)).thenReturn(true);

        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(skjermet_tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2)).thenReturn(skjermet_tpsPerson);
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterController.hentMoter(null, null, false, NAV_ENHET, false);

        assertEquals(0, moteList.size());

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test(expected = RuntimeException.class)
    public void hentMoter_navenhet_annen_tilgangsfeil() {
        when(tilgangService.harVeilederTilgangTilPersonViaAzure(ARBEIDSTAKER_FNR)).thenReturn(true);
        doThrow(new RuntimeException()).when(tilgangService).harVeilederTilgangTilPersonViaAzure(ARBEIDSTAKER_FNR);

        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2)).thenReturn(tpsPerson);
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        moterController.hentMoter(null, null, false, NAV_ENHET, false);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test
    public void opprettMoter_har_tilgang() {
        final RSNyttMoteRequest nyttMoteRequest = new RSNyttMoteRequest()
                .fnr(ARBEIDSTAKER_FNR)
                .orgnummer("123");

        when(tilgangService.harVeilederTilgangTilPersonViaAzure(ARBEIDSTAKER_FNR)).thenReturn(true);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);
        when(sykefravaersoppfoelgingService.hentNaermesteLederSomBruker(ARBEIDSTAKER_AKTORID, nyttMoteRequest.orgnummer)).thenReturn(
                new NaermesteLeder()
                        .navn("Frida Frisk")
                        .epost("frida.frisk@bedrift.no")
        );
        when(moteService.opprettMote(any())).thenReturn(new Mote().id(1L));
        when(tidOgStedDAO.create(any())).thenReturn(new TidOgSted());
        when(motedeltakerDAO.create(any(PMotedeltakerAktorId.class))).thenReturn(new MotedeltakerAktorId());
        when(motedeltakerDAO.create(any(PMotedeltakerArbeidsgiver.class))).thenReturn(new MotedeltakerArbeidsgiver());
        when(veilederService.hentVeileder(any())).thenReturn(new Veileder());

        moterController.opprett(nyttMoteRequest);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(enhetService).finnArbeidstakersBehandlendeEnhet(ARBEIDSTAKER_FNR);
    }

    @Test(expected = ForbiddenException.class)
    public void opprettMoter_ikke_tilgang_pga_skjermet_bruker() {
        when(tilgangService.harVeilederTilgangTilPersonViaAzure(ARBEIDSTAKER_FNR)).thenReturn(true);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(skjermet_tpsPerson);

        moterController.opprett(new RSNyttMoteRequest().fnr(ARBEIDSTAKER_FNR));

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
    }

    @Test(expected = ForbiddenException.class)
    public void opprettMoter_ikke_tilgang_pga_rolle() {
        when(tilgangService.harVeilederTilgangTilPersonViaAzure(ARBEIDSTAKER_FNR)).thenReturn(false);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);

        moterController.opprett(new RSNyttMoteRequest().fnr(ARBEIDSTAKER_FNR));

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
    }

    @Test(expected = RuntimeException.class)
    public void opprettMoter_annen_tilgangsfeil() {
        doThrow(new RuntimeException()).when(tilgangService).harVeilederTilgangTilPersonViaAzure(ARBEIDSTAKER_FNR);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);

        moterController.opprett(new RSNyttMoteRequest().fnr(ARBEIDSTAKER_FNR));

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
    }
}
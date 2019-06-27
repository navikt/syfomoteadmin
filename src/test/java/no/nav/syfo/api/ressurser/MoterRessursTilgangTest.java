package no.nav.syfo.api.ressurser;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.LocalApplication;
import no.nav.syfo.api.domain.RSMote;
import no.nav.syfo.api.domain.nyttmoterequest.RSNyttMoteRequest;
import no.nav.syfo.domain.model.*;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.dao.MotedeltakerDAO;
import no.nav.syfo.repository.dao.TidOgStedDAO;
import no.nav.syfo.repository.model.PMotedeltakerAktorId;
import no.nav.syfo.repository.model.PMotedeltakerArbeidsgiver;
import no.nav.syfo.service.*;
import no.nav.syfo.service.varselinnhold.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.ws.rs.ForbiddenException;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static no.nav.syfo.testhelper.OidcTestHelper.lagOIDCValidationContextIntern;
import static no.nav.syfo.testhelper.UserConstants.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class MoterRessursTilgangTest {

    private static final String AKTOER_ID_2 = "1101010101010";
    private static final String FNR_2 = "11010101010";
    private static final String NAVENHET = "1234";
    private static final TpsPerson skjermet_tpsPerson = new TpsPerson().skjermetBruker(true);
    private static final TpsPerson tpsPerson = new TpsPerson().skjermetBruker(false);

    @Mock
    public OIDCRequestContextHolder oidcRequestContextHolder;
    @Mock
    private AktoerService aktoerService;
    @Mock
    private MoteService moteService;
    @Mock
    private EnhetService enhetService;
    @Mock
    private Metrikk metrikk;
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
    private ArbeidsgiverVarselService arbeidsgiverVarselService;
    @Mock
    private SykefravaersoppfoelgingService sykefravaersoppfoelgingService;
    @Mock
    private SykmeldtVarselService sykmeldtVarselService;
    @Mock
    private TilgangService tilgangService;
    @InjectMocks
    private MoterRessurs moterRessurs;

    @Before
    public void setUp() {
        when(oidcRequestContextHolder.getOIDCValidationContext()).thenReturn(lagOIDCValidationContextIntern(VEILEDER_ID));
        when(aktoerService.hentFnrForAktoer(ARBEIDSTAKER_AKTORID)).thenReturn(ARBEIDSTAKER_FNR);
        when(aktoerService.hentAktoerIdForIdent(ARBEIDSTAKER_FNR)).thenReturn(ARBEIDSTAKER_AKTORID);
        when(aktoerService.hentFnrForAktoer(AKTOER_ID_2)).thenReturn(FNR_2);
        when(moteService.findMoterByBrukerNavEnhet(NAVENHET)).thenReturn(MoteList);
        when(enhetService.finnArbeidstakersBehandlendeEnhet(any())).thenReturn(NAVENHET);
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

        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(true);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);
        when(moteService.findMoterByBrukerAktoerId(ARBEIDSTAKER_AKTORID)).thenReturn(asList(Mote1, Mote2));
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterRessurs.hentMoter(null, ARBEIDSTAKER_FNR, false, null, false);

        assertEquals(ARBEIDSTAKER_AKTORID, moteList.get(0).aktorId);
        assertEquals(ARBEIDSTAKER_AKTORID, moteList.get(1).aktorId);

        verify(brukerprofilService, times(3)).hentBruker(ARBEIDSTAKER_FNR);
        verify(aktoerService, times(4)).hentFnrForAktoer(ARBEIDSTAKER_AKTORID);
        verify(aktoerService, times(1)).hentAktoerIdForIdent(ARBEIDSTAKER_FNR);
        verify(moteService).findMoterByBrukerAktoerId(ARBEIDSTAKER_AKTORID);
    }

    @Test(expected = ForbiddenException.class)
    public void hentMoter_fnr_veileder_har_ikke_tilgang_pga_rolle() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(false);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);

        moterRessurs.hentMoter(null, ARBEIDSTAKER_FNR, false, null, false);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
    }

    @Test(expected = ForbiddenException.class)
    public void hentMoter_fnr_veileder_har_ikke_tilgang_pga_skjermet_bruker() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(true);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(skjermet_tpsPerson);

        moterRessurs.hentMoter(null, ARBEIDSTAKER_FNR, false, null, false);
    }

    @Test(expected = RuntimeException.class)
    public void hentMoter_fnr_veileder_annen_tilgangsfeil() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(false);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);

        moterRessurs.hentMoter(null, ARBEIDSTAKER_FNR, false, null, false);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
    }

    @Test
    public void hentMoter_navenhet_veileder_har_full_tilgang() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(true);
        when(tilgangService.harVeilederTilgangTilPerson(FNR_2)).thenReturn(true);

        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR))
                .thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2))
                .thenReturn(tpsPerson);
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterRessurs.hentMoter(null, null, false, NAVENHET, false);

        assertEquals(2, moteList.size());
        assertEquals(ARBEIDSTAKER_AKTORID, moteList.get(0).aktorId);
        assertEquals(AKTOER_ID_2, moteList.get(1).aktorId);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test
    public void hentMoter_navenhet_veileder_har_delvis_tilgang_pga_rolle() {
        when(tilgangService.harVeilederTilgangTilPerson(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR))
                .thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2))
                .thenReturn(tpsPerson);
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterRessurs.hentMoter(null, null, false, NAVENHET, false);

        assertEquals(1, moteList.size());
        assertEquals(ARBEIDSTAKER_AKTORID, moteList.get(0).aktorId);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test
    public void hentMoter_navenhet_veileder_har_delvis_tilgang_pga_skjermet_bruker() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(true);

        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR))
                .thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2))
                .thenReturn(skjermet_tpsPerson);

        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterRessurs.hentMoter(null, null, false, NAVENHET, false);

        assertEquals(1, moteList.size());
        assertEquals(ARBEIDSTAKER_AKTORID, moteList.get(0).aktorId);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test
    public void hentMoter_navenhet_veileder_har_ikke_tilgang_pga_rolle() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(false);

        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2)).thenReturn(tpsPerson);

        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterRessurs.hentMoter(null, null, false, NAVENHET, false);

        assertEquals(0, moteList.size());

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test
    public void hentMoter_navenhet_veileder_har_ikke_tilgang_pga_skjerming() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(true);

        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(skjermet_tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2)).thenReturn(skjermet_tpsPerson);
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterRessurs.hentMoter(null, null, false, NAVENHET, false);

        assertEquals(0, moteList.size());

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test(expected = RuntimeException.class)
    public void hentMoter_navenhet_annen_tilgangsfeil() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(true);
        doThrow(new RuntimeException()).when(tilgangService).harVeilederTilgangTilPerson(FNR_2);

        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2)).thenReturn(tpsPerson);
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        moterRessurs.hentMoter(null, null, false, NAVENHET, false);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test
    public void opprettMoter_har_tilgang() {
        final RSNyttMoteRequest nyttMoteRequest = new RSNyttMoteRequest()
                .fnr(ARBEIDSTAKER_FNR)
                .orgnummer("123");

        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(true);

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

        moterRessurs.opprett(nyttMoteRequest);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(enhetService).finnArbeidstakersBehandlendeEnhet(ARBEIDSTAKER_FNR);
    }

    @Test(expected = ForbiddenException.class)
    public void opprettMoter_ikke_tilgang_pga_skjermet_bruker() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(true);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(skjermet_tpsPerson);

        moterRessurs.opprett(new RSNyttMoteRequest().fnr(ARBEIDSTAKER_FNR));

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
    }

    @Test(expected = ForbiddenException.class)
    public void opprettMoter_ikke_tilgang_pga_rolle() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(false);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);

        moterRessurs.opprett(new RSNyttMoteRequest().fnr(ARBEIDSTAKER_FNR));

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
    }

    @Test(expected = RuntimeException.class)
    public void opprettMoter_annen_tilgangsfeil() {
        doThrow(new RuntimeException()).when(tilgangService).harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);

        moterRessurs.opprett(new RSNyttMoteRequest().fnr(ARBEIDSTAKER_FNR));

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
    }
}

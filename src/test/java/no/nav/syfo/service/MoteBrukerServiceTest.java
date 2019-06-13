package no.nav.syfo.service;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.api.domain.bruker.BrukerMote;
import no.nav.syfo.domain.model.*;
import no.nav.syfo.util.Brukerkontekst;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MoteBrukerServiceTest {

    @Mock
    private OIDCRequestContextHolder contextHolder;

    @Mock
    private AktoerService aktoerService;

    @Mock
    private BrukerprofilService brukerprofilService;

    @Mock
    private BrukertilgangService brukertilgangService;

    @Mock
    private MoteService moteService;

    @Mock
    private MotedeltakerService motedeltakerService;

    @Mock
    private NaermesteLedersMoterService naermesteLedersMoterService;

    @Mock
    private SyketilfelleService syketilfelleService;

    @InjectMocks
    private MoteBrukerService moteBrukerService;

    private String aktorId;
    private String brukerkontekst;
    private Mote mote;

    @Before
    public void setup() {
        aktorId = "123";
        brukerkontekst = Brukerkontekst.ARBEIDSTAKER;
        mote = new Mote()
                .uuid("123-abc")
                .status(MoteStatus.OPPRETTET)
                .opprettetTidspunkt(LocalDateTime.now())
                .alternativer(singletonList(new TidOgSted()
                        .id(2L)
                        .tid(LocalDateTime.now())
                        .created(LocalDateTime.now())
                        .sted("Oslo")
                        .valgt(false)))
                .motedeltakere(singletonList(new Motedeltaker()
                        .uuid("999-zzz")
                        .svartTidspunkt(LocalDateTime.now())
                        .tidOgStedAlternativer(singletonList(new TidOgSted()
                                .id(2L)
                                .tid(LocalDateTime.now())
                                .created(LocalDateTime.now())
                                .sted("Oslo")
                                .valgt(false)))
                        .motedeltakertype("Bruker")));
    }

    @Test
    public void finner_ingen_moter_hvis_ingen_oppfolgingstilfeller() {
        when(syketilfelleService.hentNyesteOppfolgingstilfelle(any())).thenReturn(null);

        assertThat(moteBrukerService.hentSisteBrukerMoteINyesteOppfolgingstilfelle(aktorId, brukerkontekst)).isEqualTo(null);
    }

    @Test
    public void finner_ingen_moter_hvis_ingen_arbeidsgiverperiode() {
        when(syketilfelleService.hentNyesteOppfolgingstilfelle(any())).thenReturn(new OppfolgingstilfelleDTO()
                .antallBrukteDager(10)
                .oppbruktArbeidsgvierperiode(false)
                .arbeidsgiverperiode(null));

        assertThat(moteBrukerService.hentSisteBrukerMoteINyesteOppfolgingstilfelle(aktorId, brukerkontekst)).isEqualTo(null);
    }

    @Test
    public void finner_ikke_mote_hvis_det_er_opprettet_for_oppfolgingstilfellet() {
        LocalDate tiDagerSiden = LocalDate.now().minusDays(10);
        LocalDate omSeksDager = LocalDate.now().plusDays(6);
        LocalDateTime merEnnTiDagerSiden = LocalDateTime.now().minusDays(20);
        Mote moteOpprettetIOppfolgingstilfellet = mote.opprettetTidspunkt(merEnnTiDagerSiden);

        when(syketilfelleService.hentNyesteOppfolgingstilfelle(any())).thenReturn(new OppfolgingstilfelleDTO()
                .antallBrukteDager(10)
                .oppbruktArbeidsgvierperiode(false)
                .arbeidsgiverperiode(new PeriodeDTO()
                        .fom(tiDagerSiden)
                        .tom(omSeksDager)));

        when(moteService.findMoterByBrukerAktoerId(anyString())).thenReturn(singletonList(moteOpprettetIOppfolgingstilfellet));

        assertThat(moteBrukerService.hentSisteBrukerMoteINyesteOppfolgingstilfelle(aktorId, brukerkontekst)).isEqualTo(null);
    }

    @Test
    public void finner_mote_hvis_opprettet_i_oppfolgingstilfellet() {
        LocalDate tiDagerSiden = LocalDate.now().minusDays(10);
        LocalDate omSeksDager = LocalDate.now().plusDays(6);
        LocalDateTime niDagerSiden = LocalDateTime.now().minusDays(9);
        Mote moteOpprettetIOppfolgingstilfellet = mote.opprettetTidspunkt(niDagerSiden);

        when(syketilfelleService.hentNyesteOppfolgingstilfelle(any())).thenReturn(new OppfolgingstilfelleDTO()
                .antallBrukteDager(10)
                .oppbruktArbeidsgvierperiode(false)
                .arbeidsgiverperiode(new PeriodeDTO()
                        .fom(tiDagerSiden)
                        .tom(omSeksDager)));

        when(moteService.findMoterByBrukerAktoerId(anyString())).thenReturn(singletonList(moteOpprettetIOppfolgingstilfellet));

        BrukerMote funnetBrukerMote = moteBrukerService.hentSisteBrukerMoteINyesteOppfolgingstilfelle(aktorId, brukerkontekst);

        assertThat(funnetBrukerMote).isNotNull();
        assertThat(funnetBrukerMote.moteUuid).isEqualToIgnoringCase("123-abc");
    }

    @Test
    public void finner_nyeste_mote_hvis_flere_opprettet_i_oppfolgingstilfellet() {
        LocalDate tiDagerSiden = LocalDate.now().minusDays(10);
        LocalDate omSeksDager = LocalDate.now().plusDays(6);
        LocalDateTime niDagerSiden = LocalDateTime.now().minusDays(9);
        LocalDateTime femDagerSiden = LocalDateTime.now().minusDays(5);
        final String RIKTIG_UUID = "111-aaa";

        Mote moteOpprettetIOppfolgingstilfellet = mote.opprettetTidspunkt(niDagerSiden);
        Mote nyesteMote = mote
                .uuid(RIKTIG_UUID)
                .opprettetTidspunkt(femDagerSiden);

        BrukerMote moteOpprettetIOppfolgingstilfelle = new BrukerMote()
                .opprettetTidspunkt(niDagerSiden);

        when(syketilfelleService.hentNyesteOppfolgingstilfelle(any())).thenReturn(new OppfolgingstilfelleDTO()
                .antallBrukteDager(10)
                .oppbruktArbeidsgvierperiode(false)
                .arbeidsgiverperiode(new PeriodeDTO()
                        .fom(tiDagerSiden)
                        .tom(omSeksDager)));

        when(moteService.findMoterByBrukerAktoerId(anyString())).thenReturn(asList(moteOpprettetIOppfolgingstilfellet, nyesteMote));

        BrukerMote funnetBrukerMote = moteBrukerService.hentSisteBrukerMoteINyesteOppfolgingstilfelle(aktorId, brukerkontekst);

        assertThat(funnetBrukerMote).isNotNull();
        assertThat(funnetBrukerMote.moteUuid).isEqualToIgnoringCase(RIKTIG_UUID);
    }
}

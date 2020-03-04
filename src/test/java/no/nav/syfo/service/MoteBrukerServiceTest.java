package no.nav.syfo.service;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.api.domain.bruker.BrukerMote;
import no.nav.syfo.domain.model.*;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.util.Brukerkontekst;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.nav.syfo.testhelper.UserConstants.PERSON_NAVN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MoteBrukerServiceTest {

    @Mock
    private OIDCRequestContextHolder contextHolder;

    @Mock
    private AktoerService aktoerService;

    @Mock
    private BrukertilgangService brukertilgangService;

    @Mock
    private MoteService moteService;

    @Mock
    private MotedeltakerService motedeltakerService;

    @Mock
    private NaermesteLedersMoterService naermesteLedersMoterService;

    @Mock
    private PdlConsumer pdlConsumer;

    @InjectMocks
    private MoteBrukerService moteBrukerService;

    private String aktorId;
    private String brukerkontekst;
    private Mote mote;
    private LocalDateTime mottattDatoTiDagerSiden;


    @Before
    public void setup() {
        aktorId = "123";
        brukerkontekst = Brukerkontekst.ARBEIDSTAKER;
        mottattDatoTiDagerSiden = LocalDateTime.now().minusDays(10);
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
    public void hentSisteBrukerMoteEtterDato_finner_ikke_mote_hvis_det_er_opprettet_for_mottatt_dato() {
        LocalDateTime merEnnTiDagerSiden = LocalDateTime.now().minusDays(20);
        Mote nyesteMote = mote.opprettetTidspunkt(merEnnTiDagerSiden);

        when(moteService.findMoterByBrukerAktoerId(anyString())).thenReturn(singletonList(nyesteMote));

        assertThat(moteBrukerService.hentSisteBrukerMoteEtterDato(aktorId, brukerkontekst, mottattDatoTiDagerSiden).isPresent()).isFalse();
    }

    @Test
    public void hentSisteBrukerMoteEtterDato_finner_mote_hvis_opprettet_etter_mottatt_dato() {
        LocalDateTime niDagerSiden = LocalDateTime.now().minusDays(9);
        Mote nyesteMote = mote.opprettetTidspunkt(niDagerSiden);

        when(moteService.findMoterByBrukerAktoerId(anyString())).thenReturn(singletonList(nyesteMote));

        BrukerMote funnetBrukerMote = moteBrukerService.hentSisteBrukerMoteEtterDato(aktorId, brukerkontekst, mottattDatoTiDagerSiden).orElse(null);

        assertThat(funnetBrukerMote).isNotNull();
        assertThat(funnetBrukerMote.moteUuid).isEqualToIgnoringCase("123-abc");
    }

    @Test
    public void hentSisteBrukerMoteEtterDato_finner_nyeste_mote_hvis_flere_opprettet_etter_mottatt_dato() {
        LocalDateTime niDagerSiden = LocalDateTime.now().minusDays(9);
        LocalDateTime femDagerSiden = LocalDateTime.now().minusDays(5);
        final String RIKTIG_UUID = "111-aaa";

        Mote eldsteMoteOpprettetEtterMottatDato = mote.opprettetTidspunkt(niDagerSiden);
        Mote nyesteMote = mote
                .uuid(RIKTIG_UUID)
                .opprettetTidspunkt(femDagerSiden);

        when(moteService.findMoterByBrukerAktoerId(anyString())).thenReturn(asList(eldsteMoteOpprettetEtterMottatDato, nyesteMote));

        BrukerMote funnetBrukerMote = moteBrukerService.hentSisteBrukerMoteEtterDato(aktorId, brukerkontekst, mottattDatoTiDagerSiden).orElse(null);

        assertThat(funnetBrukerMote).isNotNull();
        assertThat(funnetBrukerMote.moteUuid).isEqualToIgnoringCase(RIKTIG_UUID);
    }
}

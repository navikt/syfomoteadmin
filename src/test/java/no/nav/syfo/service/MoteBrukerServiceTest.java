package no.nav.syfo.service;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.aktorregister.domain.Fodselsnummer;
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
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID;
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MoteBrukerServiceTest {

    @Mock
    private OIDCRequestContextHolder contextHolder;

    @Mock
    private AktorregisterConsumer aktorregisterConsumer;

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
        aktorId = ARBEIDSTAKER_AKTORID;
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
                .motedeltakere(singletonList(new MotedeltakerAktorId()
                        .aktorId(ARBEIDSTAKER_AKTORID)
                        .uuid("999-zzz")
                        .svartTidspunkt(LocalDateTime.now())
                        .tidOgStedAlternativer(singletonList(new TidOgSted()
                                .id(2L)
                                .tid(LocalDateTime.now())
                                .created(LocalDateTime.now())
                                .sted("Oslo")
                                .valgt(false)))
                        .motedeltakertype("Bruker")));
        when(aktorregisterConsumer.getAktorIdForFodselsnummer(new Fodselsnummer(ARBEIDSTAKER_FNR))).thenReturn(ARBEIDSTAKER_AKTORID);
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

    @Test
    public void harMoteplanleggerIBrukEtterDato_false_mote_opprettet_opprettet_for_grensedato() {
        LocalDateTime datoEldreEnnGrensedato = mottattDatoTiDagerSiden.minusDays(1);
        Mote nyesteMote = mote.opprettetTidspunkt(datoEldreEnnGrensedato);

        when(moteService.findMoterByBrukerAktoerId(anyString())).thenReturn(singletonList(nyesteMote));

        Boolean harMoteplanleggerIBrukEtterDato = moteBrukerService.harMoteplanleggerIBruk(new Fodselsnummer(ARBEIDSTAKER_FNR), brukerkontekst, mottattDatoTiDagerSiden);
        assertThat(harMoteplanleggerIBrukEtterDato).isFalse();
    }
    @Test
    public void harMoteplanleggerIBrukEtterDato_false_mote_opprettet_etter_grensedato_status_bekreftet_dato_passert() {
        LocalDateTime datoEldreEnnGrenseDato = mottattDatoTiDagerSiden.plusDays(1);
        Mote nyesteMote = mote
                .status(MoteStatus.BEKREFTET)
                .valgtTidOgSted(new TidOgSted()
                        .tid(LocalDateTime.now().minusDays(1))
                )
                .opprettetTidspunkt(datoEldreEnnGrenseDato);

        when(moteService.findMoterByBrukerAktoerId(anyString())).thenReturn(singletonList(nyesteMote));

        Boolean harMoteplanleggerIBrukEtterDato = moteBrukerService.harMoteplanleggerIBruk(new Fodselsnummer(ARBEIDSTAKER_FNR), brukerkontekst, mottattDatoTiDagerSiden);
        assertThat(harMoteplanleggerIBrukEtterDato).isFalse();
    }

    @Test
    public void harMoteplanleggerIBrukEtterDato_true_mote_opprettet_etter_grensedato_status_bekreftet_dato_ikke_passert() {
        LocalDateTime datoEldreEnnGrenseDato = mottattDatoTiDagerSiden.plusDays(1);
        Mote nyesteMote = mote
                .status(MoteStatus.BEKREFTET)
                .valgtTidOgSted(new TidOgSted()
                        .tid(LocalDateTime.now())
                )
                .opprettetTidspunkt(datoEldreEnnGrenseDato);

        when(moteService.findMoterByBrukerAktoerId(anyString())).thenReturn(singletonList(nyesteMote));

        Boolean harMoteplanleggerIBrukEtterDato = moteBrukerService.harMoteplanleggerIBruk(new Fodselsnummer(ARBEIDSTAKER_FNR), brukerkontekst, mottattDatoTiDagerSiden);
        assertThat(harMoteplanleggerIBrukEtterDato).isTrue();
    }

    @Test
    public void harMoteplanleggerIBrukEtterDato_true_hvis_mote_opprettet_etter_grensedato_status_opprettet() {
        LocalDateTime datoEldreEnnGrensedato = mottattDatoTiDagerSiden.plusDays(1);
        Mote nyesteMote = mote
                .status(MoteStatus.OPPRETTET)
                .opprettetTidspunkt(datoEldreEnnGrensedato);

        when(moteService.findMoterByBrukerAktoerId(anyString())).thenReturn(singletonList(nyesteMote));

        Boolean harMoteplanleggerIBrukEtterDato = moteBrukerService.harMoteplanleggerIBruk(new Fodselsnummer(ARBEIDSTAKER_FNR), brukerkontekst, mottattDatoTiDagerSiden);

        assertThat(harMoteplanleggerIBrukEtterDato).isTrue();
    }

    @Test
    public void harMoteplanleggerIBrukEtterDato_true_hvis_mote_opprettet_etter_grensedato_status_flere_tidspunkt() {
        LocalDateTime datoEtterGrensedato = mottattDatoTiDagerSiden.plusDays(1);
        Mote nyesteMote = mote
                .status(MoteStatus.FLERE_TIDSPUNKT)
                .opprettetTidspunkt(datoEtterGrensedato);

        when(moteService.findMoterByBrukerAktoerId(anyString())).thenReturn(singletonList(nyesteMote));

        Boolean harMoteplanleggerIBrukEtterDato = moteBrukerService.harMoteplanleggerIBruk(new Fodselsnummer(ARBEIDSTAKER_FNR), brukerkontekst, mottattDatoTiDagerSiden);

        assertThat(harMoteplanleggerIBrukEtterDato).isTrue();
    }

    @Test
    public void harMoteplanleggerIBrukEtterDato_true_hvis_flere_moter_opprettet_etter_grensedato() {
        LocalDateTime eldsteDatoEtterGrensedato = mottattDatoTiDagerSiden.plusDays(1);
        LocalDateTime nyesteDatoEtterGrensedato = mottattDatoTiDagerSiden.plusDays(5);
        final String RIKTIG_UUID = "111-aaa";

        Mote eldsteMoteOpprettetEtterMottatDato = mote
                .status(MoteStatus.OPPRETTET)
                .opprettetTidspunkt(eldsteDatoEtterGrensedato);
        Mote nyesteMote = mote
                .status(MoteStatus.OPPRETTET)
                .uuid(RIKTIG_UUID)
                .opprettetTidspunkt(nyesteDatoEtterGrensedato);

        when(moteService.findMoterByBrukerAktoerId(anyString())).thenReturn(asList(eldsteMoteOpprettetEtterMottatDato, nyesteMote));

        Boolean harMoteplanleggerIBrukEtterDato = moteBrukerService.harMoteplanleggerIBruk(new Fodselsnummer(ARBEIDSTAKER_FNR), brukerkontekst, mottattDatoTiDagerSiden);

        assertThat(harMoteplanleggerIBrukEtterDato).isTrue();
    }
}

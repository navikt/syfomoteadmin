package no.nav.syfo.service

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.consumer.aktorregister.AktorregisterConsumer
import no.nav.syfo.consumer.aktorregister.domain.Fodselsnummer
import no.nav.syfo.domain.model.*
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.util.Brukerkontekst
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDateTime

@RunWith(MockitoJUnitRunner::class)
class MoteBrukerServiceTest {
    @Mock
    private lateinit var contextHolder: OIDCRequestContextHolder

    @Mock
    private lateinit var aktorregisterConsumer: AktorregisterConsumer

    @Mock
    private lateinit var brukertilgangService: BrukertilgangService

    @Mock
    private lateinit var moteService: MoteService

    @Mock
    private lateinit var motedeltakerService: MotedeltakerService

    @Mock
    private lateinit var naermesteLedersMoterService: NaermesteLedersMoterService

    @Mock
    private lateinit var pdlConsumer: PdlConsumer

    @InjectMocks
    private lateinit var moteBrukerService: MoteBrukerService

    private var aktorId: String = ARBEIDSTAKER_AKTORID
    private var brukerkontekst: String = Brukerkontekst.ARBEIDSTAKER
    private var mote: Mote = Mote()
        .uuid("123-abc")
        .status(MoteStatus.OPPRETTET)
        .opprettetTidspunkt(LocalDateTime.now())
        .alternativer(
            listOf(TidOgSted()
                .id(2L)
                .tid(LocalDateTime.now())
                .created(LocalDateTime.now())
                .sted("Oslo")
                .valgt(false)
            )
        )
        .motedeltakere(
            listOf(
                MotedeltakerAktorId()
                    .aktorId(ARBEIDSTAKER_AKTORID)
                    .uuid("999-zzz")
                    .svartTidspunkt(LocalDateTime.now())
                    .tidOgStedAlternativer(
                        listOf(TidOgSted()
                            .id(2L)
                            .tid(LocalDateTime.now())
                            .created(LocalDateTime.now()
                            )
                            .sted("Oslo")
                            .valgt(false)
                        )
                    )
                    .motedeltakertype("Bruker")
            )
        )
    private var mottattDatoTiDagerSiden: LocalDateTime = LocalDateTime.now().minusDays(10)

    @Before
    fun setup() {
        Mockito.`when`(aktorregisterConsumer.getAktorIdForFodselsnummer(Fodselsnummer(ARBEIDSTAKER_FNR))).thenReturn(ARBEIDSTAKER_AKTORID)
    }

    @Test
    fun hentSisteBrukerMoteEtterDato_finner_ikke_mote_hvis_det_er_opprettet_for_mottatt_dato() {
        val merEnnTiDagerSiden = LocalDateTime.now().minusDays(20)
        val nyesteMote = mote.opprettetTidspunkt(merEnnTiDagerSiden)
        Mockito.`when`(moteService.findMoterByBrukerAktoerId(ArgumentMatchers.anyString())).thenReturn(listOf(nyesteMote))
        Assertions.assertThat(moteBrukerService.hentSisteBrukerMoteEtterDato(aktorId, brukerkontekst, mottattDatoTiDagerSiden).isPresent).isFalse
    }

    @Test
    fun hentSisteBrukerMoteEtterDato_finner_mote_hvis_opprettet_etter_mottatt_dato() {
        val niDagerSiden = LocalDateTime.now().minusDays(9)
        val nyesteMote = mote.opprettetTidspunkt(niDagerSiden)
        Mockito.`when`(moteService.findMoterByBrukerAktoerId(ArgumentMatchers.anyString())).thenReturn(listOf(nyesteMote))
        val funnetBrukerMote = moteBrukerService.hentSisteBrukerMoteEtterDato(aktorId, brukerkontekst, mottattDatoTiDagerSiden).orElse(null)
        Assertions.assertThat(funnetBrukerMote).isNotNull
        Assertions.assertThat(funnetBrukerMote.moteUuid).isEqualToIgnoringCase("123-abc")
    }

    @Test
    fun hentSisteBrukerMoteEtterDato_finner_nyeste_mote_hvis_flere_opprettet_etter_mottatt_dato() {
        val niDagerSiden = LocalDateTime.now().minusDays(9)
        val femDagerSiden = LocalDateTime.now().minusDays(5)
        val RIKTIG_UUID = "111-aaa"
        val eldsteMoteOpprettetEtterMottatDato = mote.opprettetTidspunkt(niDagerSiden)
        val nyesteMote = mote
            .uuid(RIKTIG_UUID)
            .opprettetTidspunkt(femDagerSiden)
        Mockito.`when`(moteService.findMoterByBrukerAktoerId(ArgumentMatchers.anyString())).thenReturn(listOf(eldsteMoteOpprettetEtterMottatDato, nyesteMote))
        val funnetBrukerMote = moteBrukerService.hentSisteBrukerMoteEtterDato(aktorId, brukerkontekst, mottattDatoTiDagerSiden).orElse(null)
        Assertions.assertThat(funnetBrukerMote).isNotNull
        Assertions.assertThat(funnetBrukerMote.moteUuid).isEqualToIgnoringCase(RIKTIG_UUID)
    }

    @Test
    fun harMoteplanleggerIBrukEtterDato_false_mote_opprettet_opprettet_for_grensedato() {
        val datoEldreEnnGrensedato = mottattDatoTiDagerSiden.minusDays(1)
        val nyesteMote = mote.opprettetTidspunkt(datoEldreEnnGrensedato)
        Mockito.`when`(moteService.findMoterByBrukerAktoerId(ArgumentMatchers.anyString())).thenReturn(listOf(nyesteMote))
        val harMoteplanleggerIBrukEtterDato = moteBrukerService.harMoteplanleggerIBruk(Fodselsnummer(ARBEIDSTAKER_FNR), brukerkontekst, mottattDatoTiDagerSiden)
        Assertions.assertThat(harMoteplanleggerIBrukEtterDato).isFalse
    }

    @Test
    fun harMoteplanleggerIBrukEtterDato_false_mote_opprettet_etter_grensedato_status_bekreftet_dato_passert() {
        val datoEldreEnnGrenseDato = mottattDatoTiDagerSiden.plusDays(1)
        val nyesteMote = mote
            .status(MoteStatus.BEKREFTET)
            .valgtTidOgSted(TidOgSted()
                .tid(LocalDateTime.now().minusDays(1))
            )
            .opprettetTidspunkt(datoEldreEnnGrenseDato)
        Mockito.`when`(moteService.findMoterByBrukerAktoerId(ArgumentMatchers.anyString())).thenReturn(listOf(nyesteMote))
        val harMoteplanleggerIBrukEtterDato = moteBrukerService.harMoteplanleggerIBruk(Fodselsnummer(ARBEIDSTAKER_FNR), brukerkontekst, mottattDatoTiDagerSiden)
        Assertions.assertThat(harMoteplanleggerIBrukEtterDato).isFalse
    }

    @Test
    fun harMoteplanleggerIBrukEtterDato_true_mote_opprettet_etter_grensedato_status_bekreftet_dato_ikke_passert() {
        val datoEldreEnnGrenseDato = mottattDatoTiDagerSiden.plusDays(1)
        val nyesteMote = mote
            .status(MoteStatus.BEKREFTET)
            .valgtTidOgSted(TidOgSted()
                .tid(LocalDateTime.now())
            )
            .opprettetTidspunkt(datoEldreEnnGrenseDato)
        Mockito.`when`(moteService.findMoterByBrukerAktoerId(ArgumentMatchers.anyString())).thenReturn(listOf(nyesteMote))
        val harMoteplanleggerIBrukEtterDato = moteBrukerService.harMoteplanleggerIBruk(Fodselsnummer(ARBEIDSTAKER_FNR), brukerkontekst, mottattDatoTiDagerSiden)
        Assertions.assertThat(harMoteplanleggerIBrukEtterDato).isTrue
    }

    @Test
    fun harMoteplanleggerIBrukEtterDato_true_hvis_mote_opprettet_etter_grensedato_status_opprettet() {
        val datoEldreEnnGrensedato = mottattDatoTiDagerSiden.plusDays(1)
        val nyesteMote = mote
            .status(MoteStatus.OPPRETTET)
            .opprettetTidspunkt(datoEldreEnnGrensedato)
        Mockito.`when`(moteService.findMoterByBrukerAktoerId(ArgumentMatchers.anyString())).thenReturn(listOf(nyesteMote))
        val harMoteplanleggerIBrukEtterDato = moteBrukerService.harMoteplanleggerIBruk(Fodselsnummer(ARBEIDSTAKER_FNR), brukerkontekst, mottattDatoTiDagerSiden)
        Assertions.assertThat(harMoteplanleggerIBrukEtterDato).isTrue
    }

    @Test
    fun harMoteplanleggerIBrukEtterDato_true_hvis_mote_opprettet_etter_grensedato_status_flere_tidspunkt() {
        val datoEtterGrensedato = mottattDatoTiDagerSiden.plusDays(1)
        val nyesteMote = mote
            .status(MoteStatus.FLERE_TIDSPUNKT)
            .opprettetTidspunkt(datoEtterGrensedato)
        Mockito.`when`(moteService.findMoterByBrukerAktoerId(ArgumentMatchers.anyString())).thenReturn(listOf(nyesteMote))
        val harMoteplanleggerIBrukEtterDato = moteBrukerService.harMoteplanleggerIBruk(Fodselsnummer(ARBEIDSTAKER_FNR), brukerkontekst, mottattDatoTiDagerSiden)
        Assertions.assertThat(harMoteplanleggerIBrukEtterDato).isTrue
    }

    @Test
    fun harMoteplanleggerIBrukEtterDato_true_hvis_flere_moter_opprettet_etter_grensedato() {
        val eldsteDatoEtterGrensedato = mottattDatoTiDagerSiden.plusDays(1)
        val nyesteDatoEtterGrensedato = mottattDatoTiDagerSiden.plusDays(5)
        val RIKTIG_UUID = "111-aaa"
        val eldsteMoteOpprettetEtterMottatDato = mote
            .status(MoteStatus.OPPRETTET)
            .opprettetTidspunkt(eldsteDatoEtterGrensedato)
        val nyesteMote = mote
            .status(MoteStatus.OPPRETTET)
            .uuid(RIKTIG_UUID)
            .opprettetTidspunkt(nyesteDatoEtterGrensedato)
        Mockito.`when`(moteService.findMoterByBrukerAktoerId(ArgumentMatchers.anyString())).thenReturn(listOf(eldsteMoteOpprettetEtterMottatDato, nyesteMote))
        val harMoteplanleggerIBrukEtterDato = moteBrukerService.harMoteplanleggerIBruk(Fodselsnummer(ARBEIDSTAKER_FNR), brukerkontekst, mottattDatoTiDagerSiden)
        Assertions.assertThat(harMoteplanleggerIBrukEtterDato).isTrue
    }
}

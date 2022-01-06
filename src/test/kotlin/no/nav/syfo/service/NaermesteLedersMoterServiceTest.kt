package no.nav.syfo.service

import no.nav.syfo.consumer.narmesteleder.*
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.domain.model.Mote
import no.nav.syfo.domain.model.TidOgSted
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID2
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR2
import no.nav.syfo.testhelper.UserConstants.LEDER_AKTORID
import no.nav.syfo.testhelper.UserConstants.LEDER_FNR
import no.nav.syfo.testhelper.UserConstants.PERSON_EMAIL
import no.nav.syfo.testhelper.UserConstants.PERSON_TLF
import no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER
import no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER2
import no.nav.syfo.testhelper.UserConstants.VIRKSOMHET_NAME1
import no.nav.syfo.testhelper.moterServiceMockRelasjon
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class NaermesteLedersMoterServiceTest {
    @Mock
    private lateinit var narmesteLederConsumer: NarmesteLederConsumer

    @Mock
    private lateinit var moteService: MoteService

    @Mock
    private lateinit var pdlConsumer: PdlConsumer

    @InjectMocks
    private lateinit var naermesteLedersMoterService: NaermesteLedersMoterService


    @Test
    @Throws(Exception::class)
    fun hentNaermeteLedersMoter() {
        Mockito.`when`(pdlConsumer.fodselsnummer(AktorId(LEDER_AKTORID))).thenReturn(Fodselsnummer(LEDER_FNR))
        Mockito.`when`(pdlConsumer.aktorId(Fodselsnummer(ARBEIDSTAKER_FNR))).thenReturn(AktorId(ARBEIDSTAKER_AKTORID))
        Mockito.`when`(pdlConsumer.aktorId(Fodselsnummer(ARBEIDSTAKER_FNR2))).thenReturn(AktorId(ARBEIDSTAKER_AKTORID2))
        Mockito.`when`(narmesteLederConsumer.getAnsatteUsingSystemToken(LEDER_FNR)).thenReturn(
            ArrayList(
                listOf(
                    moterServiceMockRelasjon,
                    NarmesteLederRelasjonDTO(
                        "uuid",
                        ARBEIDSTAKER_FNR2,
                        VIRKSOMHET_NAME1,
                        VIRKSOMHETSNUMMER2,
                        LEDER_FNR,
                        PERSON_TLF,
                        PERSON_EMAIL,
                        "Leder Navnesen",
                        LocalDate.of(2017, 3, 2),
                        LocalDate.of(2018, 3, 2),
                        false,
                        LocalDateTime.now().minusYears(3),
                        "INNMELDT_AKTIV",
                    )
                )))
        Mockito.`when`(moteService.findMoterByBrukerAktoerIdOgAGOrgnummer(ARBEIDSTAKER_AKTORID, VIRKSOMHETSNUMMER)).thenReturn(
            ArrayList(listOf(
                Mote()
                    .id(1L)
                    .alternativer(listOf(
                        TidOgSted()
                            .tid(LocalDateTime.of(2017, 3, 1, 12, 13)),
                        TidOgSted()
                            .tid(LocalDateTime.of(2017, 3, 2, 12, 13))
                    )),
                Mote()
                    .id(3L)
                    .alternativer(listOf(
                        TidOgSted()
                            .tid(LocalDateTime.of(2017, 3, 1, 12, 13)),
                        TidOgSted()
                            .tid(LocalDateTime.of(2017, 3, 2, 12, 13))
                    )))))
        Mockito.`when`(moteService.findMoterByBrukerAktoerIdOgAGOrgnummer(ARBEIDSTAKER_AKTORID2, VIRKSOMHETSNUMMER2)).thenReturn(
            ArrayList(listOf(
                Mote()
                    .id(2L)
                    .alternativer(listOf(
                        TidOgSted()
                            .tid(LocalDateTime.of(2017, 3, 1, 12, 13)),
                        TidOgSted()
                            .tid(LocalDateTime.of(2017, 3, 2, 12, 13))
                    )))))
        val moter = naermesteLedersMoterService.hentNarmesteLedersMoter(LEDER_AKTORID)
        Assertions.assertThat(moter).hasSize(3)
        Assertions.assertThat(moter[0].id).isEqualTo(1L)
        Assertions.assertThat(moter[1].id).isEqualTo(3L)
        Assertions.assertThat(moter[2].id).isEqualTo(2L)
    }

    @Test
    @Throws(Exception::class)
    fun hentNaermeteLedersMoterToGamleMoter() {
        Mockito.`when`(pdlConsumer.fodselsnummer(AktorId(LEDER_AKTORID))).thenReturn(Fodselsnummer(LEDER_FNR))
        Mockito.`when`(pdlConsumer.aktorId(Fodselsnummer(ARBEIDSTAKER_FNR))).thenReturn(AktorId(ARBEIDSTAKER_AKTORID))
        Mockito.`when`(pdlConsumer.aktorId(Fodselsnummer(ARBEIDSTAKER_FNR2))).thenReturn(AktorId(ARBEIDSTAKER_AKTORID2))
        Mockito.`when`(narmesteLederConsumer.getAnsatteUsingSystemToken(LEDER_FNR)).thenReturn(
            ArrayList(
                listOf(
                    moterServiceMockRelasjon,
                    NarmesteLederRelasjonDTO(
                        "uuid",
                        ARBEIDSTAKER_FNR2,
                        VIRKSOMHET_NAME1,
                        VIRKSOMHETSNUMMER2,
                        LEDER_FNR,
                        PERSON_TLF,
                        PERSON_EMAIL,
                        "Leder Navnesen",
                        LocalDate.of(2017, 3, 3),
                        LocalDate.of(2018, 3, 3),
                        false,
                        LocalDateTime.now().minusYears(3),
                        "INNMELDT_AKTIV",
                    )
                )))
        Mockito.`when`(moteService.findMoterByBrukerAktoerIdOgAGOrgnummer(ARBEIDSTAKER_AKTORID, VIRKSOMHETSNUMMER)).thenReturn(
            ArrayList(listOf(
                Mote()
                    .id(1L)
                    .alternativer(listOf(
                        TidOgSted()
                            .tid(LocalDateTime.of(2017, 2, 28, 12, 13)),
                        TidOgSted()
                            .tid(LocalDateTime.of(2017, 3, 1, 12, 13))
                    )),
                Mote()
                    .id(3L)
                    .alternativer(listOf(
                        TidOgSted()
                            .tid(LocalDateTime.of(2017, 3, 1, 12, 13)),
                        TidOgSted()
                            .tid(LocalDateTime.of(2017, 3, 2, 12, 13))
                    )))))
        Mockito.`when`(moteService.findMoterByBrukerAktoerIdOgAGOrgnummer(ARBEIDSTAKER_AKTORID2, VIRKSOMHETSNUMMER2)).thenReturn(
            ArrayList(listOf(
                Mote()
                    .id(2L)
                    .alternativer(listOf(
                        TidOgSted()
                            .tid(LocalDateTime.of(2017, 3, 1, 12, 13)),
                        TidOgSted()
                            .tid(LocalDateTime.of(2017, 3, 2, 12, 13))
                    )))))
        val moter = naermesteLedersMoterService.hentNarmesteLedersMoter(LEDER_AKTORID)
        Assertions.assertThat(moter).hasSize(1)
        Assertions.assertThat(moter[0].id).isEqualTo(3L)
    }

    @Test
    @Throws(Exception::class)
    fun hentNaermeteLedersMoterIngenAnsatte() {
        Mockito.`when`(pdlConsumer.fodselsnummer(AktorId(LEDER_AKTORID))).thenReturn(Fodselsnummer(LEDER_FNR))
        Mockito.`when`(narmesteLederConsumer.getAnsatteUsingSystemToken(LEDER_FNR)).thenReturn(emptyList())
        val moter = naermesteLedersMoterService.hentNarmesteLedersMoter(LEDER_AKTORID)
        Assertions.assertThat(moter).isEmpty()
    }

    @Test
    @Throws(Exception::class)
    fun hentNaermeteLedersMoterIngenMoter() {
        Mockito.`when`(pdlConsumer.fodselsnummer(AktorId(LEDER_AKTORID))).thenReturn(Fodselsnummer(LEDER_FNR))
        Mockito.`when`(pdlConsumer.aktorId(Fodselsnummer(ARBEIDSTAKER_FNR))).thenReturn(AktorId(ARBEIDSTAKER_AKTORID))
        Mockito.`when`(pdlConsumer.aktorId(Fodselsnummer(ARBEIDSTAKER_FNR2))).thenReturn(AktorId(ARBEIDSTAKER_AKTORID2))
        Mockito.`when`(narmesteLederConsumer.getAnsatteUsingSystemToken(LEDER_FNR)).thenReturn(
            ArrayList(
                listOf(
                    moterServiceMockRelasjon,
                    NarmesteLederRelasjonDTO(
                        "uuid",
                        ARBEIDSTAKER_FNR2,
                        VIRKSOMHET_NAME1,
                        VIRKSOMHETSNUMMER2,
                        LEDER_FNR,
                        PERSON_TLF,
                        PERSON_EMAIL,
                        "Leder Navnesen",
                        LocalDate.of(2017, 3, 3),
                        LocalDate.of(2018, 3, 3),
                        false,
                        LocalDateTime.now().minusYears(3),
                        "INNMELDT_AKTIV",
                    )
                )))
        Mockito.`when`(moteService.findMoterByBrukerAktoerIdOgAGOrgnummer(ArgumentMatchers.startsWith(
            ARBEIDSTAKER_AKTORID), ArgumentMatchers.startsWith(VIRKSOMHETSNUMMER))).thenReturn(emptyList())
        Mockito.`when`(moteService.findMoterByBrukerAktoerIdOgAGOrgnummer(ArgumentMatchers.startsWith(
            ARBEIDSTAKER_AKTORID2), ArgumentMatchers.startsWith(VIRKSOMHETSNUMMER2))).thenReturn(emptyList())
        val moter = naermesteLedersMoterService.hentNarmesteLedersMoter(LEDER_AKTORID)
        Assertions.assertThat(moter).isEmpty()
    }
}

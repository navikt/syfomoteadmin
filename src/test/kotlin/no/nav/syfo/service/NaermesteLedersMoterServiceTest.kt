package no.nav.syfo.service

import no.nav.syfo.domain.model.Mote
import no.nav.syfo.domain.model.TidOgSted
import no.nav.syfo.consumer.narmesteleder.NarmesteLederConsumer
import no.nav.syfo.consumer.narmesteleder.NarmesteLederRelasjon
import no.nav.syfo.testhelper.UserConstants.LEDER_AKTORID
import no.nav.syfo.testhelper.generateNarmesteLederRelasjon
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

    @InjectMocks
    private lateinit var naermesteLedersMoterService: NaermesteLedersMoterService

    @Test
    @Throws(Exception::class)
    fun hentNaermeteLedersMoter() {
        Mockito.`when`(narmesteLederConsumer.narmestelederRelasjonerAnsatte("nlAktoerId")).thenReturn(
            ArrayList(
                listOf(
                    generateNarmesteLederRelasjon(),
                    NarmesteLederRelasjon(
                        "aktoerId2",
                        "orgnummer2",
                        LEDER_AKTORID,
                        null,
                        null,
                        LocalDate.of(2017, 3, 2),
                        false,
                        false,
                        emptyList()
                    ))))
        Mockito.`when`(moteService.findMoterByBrukerAktoerIdOgAGOrgnummer("aktoerId1", "orgnummer1")).thenReturn(
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
        Mockito.`when`(moteService.findMoterByBrukerAktoerIdOgAGOrgnummer("aktoerId2", "orgnummer2")).thenReturn(
            ArrayList(listOf(
                Mote()
                    .id(2L)
                    .alternativer(listOf(
                        TidOgSted()
                            .tid(LocalDateTime.of(2017, 3, 1, 12, 13)),
                        TidOgSted()
                            .tid(LocalDateTime.of(2017, 3, 2, 12, 13))
                    )))))
        val moter = naermesteLedersMoterService.hentNaermesteLedersMoter("nlAktoerId")
        Assertions.assertThat(moter).hasSize(3)
        Assertions.assertThat(moter[0].id).isEqualTo(1L)
        Assertions.assertThat(moter[1].id).isEqualTo(3L)
        Assertions.assertThat(moter[2].id).isEqualTo(2L)
    }

    @Test
    @Throws(Exception::class)
    fun hentNaermeteLedersMoterToGamleMoter() {
        Mockito.`when`(narmesteLederConsumer.narmestelederRelasjonerAnsatte("nlAktoerId")).thenReturn(
            ArrayList(
                listOf(
                    generateNarmesteLederRelasjon(),
                    NarmesteLederRelasjon(
                        "aktoerId2",
                        "orgnummer2",
                        LEDER_AKTORID,
                        null,
                        null,
                        LocalDate.of(2017, 3, 3),
                        false,
                        false,
                        emptyList()
                    ))))
        Mockito.`when`(moteService.findMoterByBrukerAktoerIdOgAGOrgnummer("aktoerId1", "orgnummer1")).thenReturn(
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
        Mockito.`when`(moteService.findMoterByBrukerAktoerIdOgAGOrgnummer("aktoerId2", "orgnummer2")).thenReturn(
            ArrayList(listOf(
                Mote()
                    .id(2L)
                    .alternativer(listOf(
                        TidOgSted()
                            .tid(LocalDateTime.of(2017, 3, 1, 12, 13)),
                        TidOgSted()
                            .tid(LocalDateTime.of(2017, 3, 2, 12, 13))
                    )))))
        val moter = naermesteLedersMoterService.hentNaermesteLedersMoter("nlAktoerId")
        Assertions.assertThat(moter).hasSize(1)
        Assertions.assertThat(moter[0].id).isEqualTo(3L)
    }

    @Test
    @Throws(Exception::class)
    fun hentNaermeteLedersMoterIngenAnsatte() {
        Mockito.`when`(narmesteLederConsumer.narmestelederRelasjonerAnsatte("nlAktoerId")).thenReturn(emptyList())
        val moter = naermesteLedersMoterService.hentNaermesteLedersMoter("nlAktoerId")
        Assertions.assertThat(moter).isEmpty()
    }

    @Test
    @Throws(Exception::class)
    fun hentNaermeteLedersMoterIngenMoter() {
        Mockito.`when`(narmesteLederConsumer.narmestelederRelasjonerAnsatte("nlAktoerId")).thenReturn(
            ArrayList(
                listOf(
                    generateNarmesteLederRelasjon(),
                    NarmesteLederRelasjon(
                        "aktoerId2",
                        "orgnummer2",
                        LEDER_AKTORID,
                        null,
                        null,
                        LocalDate.of(2017, 3, 3),
                        false,
                        false,
                        emptyList()
                    ))))
        Mockito.`when`(moteService.findMoterByBrukerAktoerIdOgAGOrgnummer(ArgumentMatchers.startsWith("aktoerId"), ArgumentMatchers.startsWith("orgnummer"))).thenReturn(emptyList())
        val moter = naermesteLedersMoterService.hentNaermesteLedersMoter("nlAktoerId")
        Assertions.assertThat(moter).isEmpty()
    }
}

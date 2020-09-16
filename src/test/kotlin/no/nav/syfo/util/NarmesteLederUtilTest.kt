package no.nav.syfo.util

import no.nav.syfo.domain.model.Mote
import no.nav.syfo.domain.model.MotedeltakerArbeidsgiver
import no.nav.syfo.narmesteleder.NarmesteLederRelasjon
import no.nav.syfo.testhelper.generateNarmesteLederRelasjon
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.time.*

@RunWith(MockitoJUnitRunner::class)
class NarmesteLederUtilTest {
    val ORGNUMMER = "12345678"
    val WRONG_ORGNUMMER = "99988877"
    val FIVE_DAYS_AGO = LocalDate.now().minusDays(5)
    val TEN_DAYS_AGO = LocalDate.now().minusDays(10)
    val FIFTEEN_DAYS_AGO = LocalDate.now().minusDays(15)
    val TWENTY_DAYS_AGO = LocalDate.now().minusDays(20)

    @Test(expected = RuntimeException::class)
    fun `Throw Exception when no leader is found`() {
        val emptyLeaderList: List<NarmesteLederRelasjon> = emptyList()
        val mote = mockMote

        narmesteLederForMeeting(emptyLeaderList, mote)
    }

    @Test(expected = RuntimeException::class)
    fun `Throw Exception when all leaders are from wrong virksomhet`() {
        val ledere = listOf(generateNarmesteLederRelasjon(orgnummer = WRONG_ORGNUMMER))
        val mote = mockMote

        narmesteLederForMeeting(ledere, mote)
    }

    @Test
    fun `Return correct leader when only one leader`() {
        val correctLeader = generateNarmesteLederRelasjon(orgnummer = ORGNUMMER, aktivFom = FIFTEEN_DAYS_AGO)
        val ledere = listOf(correctLeader)
        val mote = mockMote

        val actualNarmesteLeder = narmesteLederForMeeting(ledere, mote)

        assertThat(actualNarmesteLeder).isEqualTo(correctLeader)
    }

    @Test
    fun `Return correct leader when one leader is old enough, and one has aktivFom after mote is created`() {
        val correctLeader = generateNarmesteLederRelasjon(orgnummer = ORGNUMMER, aktivFom = FIFTEEN_DAYS_AGO)
        val wrongLeader = generateNarmesteLederRelasjon(orgnummer = ORGNUMMER, aktivFom = FIVE_DAYS_AGO)
        val ledere = listOf(wrongLeader, correctLeader)
        val mote = mockMote

        val actualNarmesteLeder = narmesteLederForMeeting(ledere, mote)

        assertThat(actualNarmesteLeder).isEqualTo(correctLeader)
    }

    @Test
    fun `Return correct leader when both leaders have aktivFom before mote was created`() {
        val correctLeader = generateNarmesteLederRelasjon(orgnummer = ORGNUMMER, aktivFom = FIFTEEN_DAYS_AGO)
        val wrongLeader = generateNarmesteLederRelasjon(orgnummer = ORGNUMMER, aktivFom = TWENTY_DAYS_AGO)
        val ledere = listOf(wrongLeader, correctLeader)
        val mote = mockMote

        val actualNarmesteLeder = narmesteLederForMeeting(ledere, mote)

        assertThat(actualNarmesteLeder).isEqualTo(correctLeader)
    }

    private val mockMote: Mote = Mote()
            .motedeltakere(listOf(MotedeltakerArbeidsgiver().orgnummer(ORGNUMMER)))
            .opprettetTidspunkt(LocalDateTime.of(TEN_DAYS_AGO, LocalTime.MIN))

}

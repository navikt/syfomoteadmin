package no.nav.syfo.util

import no.nav.syfo.consumer.narmesteleder.NarmesteLederRelasjonDTO
import no.nav.syfo.domain.model.Mote
import no.nav.syfo.domain.model.MotedeltakerArbeidsgiver
import no.nav.syfo.testhelper.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.time.*

@RunWith(MockitoJUnitRunner::class)
class NarmesteLederUtilTest {
    val FIVE_DAYS_AGO = LocalDate.now().minusDays(5)
    val TEN_DAYS_AGO = LocalDate.now().minusDays(10)
    val FIFTEEN_DAYS_AGO = LocalDate.now().minusDays(15)

    @Test(expected = RuntimeException::class)
    fun `Throw Exception when no leader is found`() {
        val emptyLeaderList: List<NarmesteLederRelasjonDTO> = emptyList()
        val mote = mockMote

        narmesteLederForMeeting(emptyLeaderList, mote)
    }

    @Test(expected = RuntimeException::class)
    fun `Throw Exception when all leaders are from wrong virksomhet`() {
        val ledere = listOf(activeLederWrongVirksomhet)
        val mote = mockMote

        narmesteLederForMeeting(ledere, mote)
    }

    @Test
    fun `Return correct leader when only one leader`() {
        val ledere = lederListWithActiveLeder
        val mote = mockMote

        val actualNarmesteLeder = narmesteLederForMeeting(ledere, mote)

        assertThat(actualNarmesteLeder).isEqualTo(activeLeder)
    }

    @Test
    fun `Return correct leader when one leader is old enough, and one has aktivFom after mote is created`() {
        val correctLeader = activeLeder
        val tooRecentLeder = activeLeder.copy(aktivFom = FIVE_DAYS_AGO)
        val ledere = listOf(correctLeader, tooRecentLeder)
        val mote = mockMote

        val actualNarmesteLeder = narmesteLederForMeeting(ledere, mote)

        assertThat(actualNarmesteLeder).isEqualTo(correctLeader)
    }

    @Test
    fun `Return correct leader when both leaders have aktivFom before mote was created`() {
        val tooEarlyLeder = activeLeder
        val correctLeder = activeLeder.copy(aktivFom = FIFTEEN_DAYS_AGO)

        val ledere = listOf(tooEarlyLeder, correctLeder)
        val mote = mockMote

        val actualNarmesteLeder = narmesteLederForMeeting(ledere, mote)

        assertThat(actualNarmesteLeder).isEqualTo(correctLeder)
    }

    private val mockMote: Mote = Mote()
        .motedeltakere(listOf(MotedeltakerArbeidsgiver().orgnummer(activeVirksomhetsnummer)))
        .opprettetTidspunkt(LocalDateTime.of(TEN_DAYS_AGO, LocalTime.MIN))

}

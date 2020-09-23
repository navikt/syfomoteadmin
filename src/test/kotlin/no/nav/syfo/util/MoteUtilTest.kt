package no.nav.syfo.util

import no.nav.syfo.domain.model.Mote
import no.nav.syfo.domain.model.TidOgSted
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDateTime


@RunWith(MockitoJUnitRunner::class)
class MoteUtilTest {

    val NOW = LocalDateTime.now()
    val TEN_DAYS_AGO = LocalDateTime.now().minusDays(10)
    val TEN_DAYS_FROM_NOW = LocalDateTime.now().plusDays(10)

    @Test
    fun `moterWithTidAfterGivenDate Return empty list when empty list is provided`() {
        val emptyMoterList = emptyList<Mote>()
        val actualMoteList = moterAfterGivenDate(emptyMoterList, NOW)

        assertThat(actualMoteList.size).isEqualTo(0)
    }

    @Test
    fun `moterWithTidAfterGivenDate Return empty list when all moter is before given date`() {
        val moterWithOldTid = listOf(createMoteWithGivenTid(TEN_DAYS_AGO))
        val actualMoteList = moterAfterGivenDate(moterWithOldTid, NOW)

        assertThat(actualMoteList.size).isEqualTo(0)
    }

    @Test
    fun `moterWithTidAfterGivenDate Return list with one element when one mote is after given date`() {
        val moterWithNewTid = listOf(createMoteWithGivenTid(TEN_DAYS_FROM_NOW))
        val actualMoteList = moterAfterGivenDate(moterWithNewTid, NOW)

        assertThat(actualMoteList.size).isEqualTo(1)
    }

    @Test
    fun `moterWithTidAfterGivenDate Return list with one element when one mote is after, and one before, given date`() {
        val moterWithMixedTid = listOf(
                createMoteWithGivenTid(TEN_DAYS_FROM_NOW),
                createMoteWithGivenTid(TEN_DAYS_AGO)
        )
        val actualMoteList = moterAfterGivenDate(moterWithMixedTid, NOW)

        assertThat(actualMoteList.size).isEqualTo(1)
    }

    @Test
    fun `newestTidFromMoteAlternativ gives newest tid from mote alternativ`() {
        val oldAlternativ = TidOgSted().tid(TEN_DAYS_AGO)
        val newAlternativ = TidOgSted().tid(TEN_DAYS_FROM_NOW)
        val moteWithMixedAlternativer = Mote()
                .alternativer(listOf(
                        oldAlternativ,
                        newAlternativ
                ))

        val actualTid = newestTidFromMoteAlternativ(moteWithMixedAlternativer)

        assertThat(actualTid).isEqualTo(TEN_DAYS_FROM_NOW)
    }
}

fun createMoteWithGivenTid(tid: LocalDateTime): Mote {
    return Mote()
            .alternativer(listOf(TidOgSted()
                    .tid(tid)))
}

package no.nav.syfo.util

import no.nav.syfo.api.domain.RSMote
import no.nav.syfo.api.domain.RSTidOgSted
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDateTime


@RunWith(MockitoJUnitRunner::class)
class MoteUtilTest {

    val NOW = LocalDateTime.now()
    val ONE_SECOND_AGO = LocalDateTime.now().minusSeconds(1)
    val TEN_DAYS_AGO = LocalDateTime.now().minusDays(10)
    val TEN_DAYS_FROM_NOW = LocalDateTime.now().plusDays(10)

    @Test
    fun `Return true when the date for the newest alternative is passed`() {
        val moteMedPassertTid = createMoteWithGivenTid(TEN_DAYS_AGO)

        val erMotePassert = erRSMotePassert(moteMedPassertTid)

        Assertions.assertThat(erMotePassert).isTrue()
    }

    @Test
    fun `Return false when the date for the newest alternative is in the future`() {
        val moteWithTimeInTheFuture = createMoteWithGivenTid(TEN_DAYS_FROM_NOW)

        val erMotePassert = erRSMotePassert(moteWithTimeInTheFuture)

        Assertions.assertThat(erMotePassert).isFalse()
    }

    @Test
    fun `Return true when the date for the newest alternative is earlier today`() {
        val moteWithTimeAtMidnight = createMoteWithGivenTid(ONE_SECOND_AGO)

        val erMotePassert = erRSMotePassert(moteWithTimeAtMidnight)

        Assertions.assertThat(erMotePassert).isTrue()
    }

    @Test
    fun `Return true when the date for the newest alternative is passed, even if the date for an older alternative is in the future`() {
        val oldestAlternative = RSTidOgSted()
                .created(TEN_DAYS_AGO)
                .tid(TEN_DAYS_FROM_NOW)

        val newestAlternative = RSTidOgSted()
                .created(NOW)
                .tid(TEN_DAYS_AGO)

        val moteWithMoreAlternatives = RSMote()
                .alternativer(listOf(
                        oldestAlternative,
                        newestAlternative
                ))

        val erMotePassert = erRSMotePassert(moteWithMoreAlternatives)

        Assertions.assertThat(erMotePassert).isTrue()
    }
}

fun createMoteWithGivenTid(tid: LocalDateTime): RSMote {
    return RSMote()
            .alternativer(listOf(RSTidOgSted()
                    .tid(tid)))
}

package no.nav.syfo.service

import no.nav.syfo.domain.model.MotedeltakerAktorId
import no.nav.syfo.util.MoterUtil
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDateTime

@RunWith(MockitoJUnitRunner::class)
class MoterUtilTest {
    @Test
    fun sisteSvarErMottatt() {
        val erSisteSvarMottatt = MoterUtil.erSisteSvarMottatt(
            listOf(
                MotedeltakerAktorId().svartTidspunkt(LocalDateTime.now())
            )
        )
        Assertions.assertThat(erSisteSvarMottatt).isTrue
    }

    @Test
    fun sisteSvarErIkkeMottatt() {
        val erSisteSvarMottatt = MoterUtil.erSisteSvarMottatt(
            listOf(
                MotedeltakerAktorId().svartTidspunkt(null)
            )
        )
        Assertions.assertThat(erSisteSvarMottatt).isFalse
    }
}

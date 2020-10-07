package no.nav.syfo.service

import no.nav.syfo.util.DatoService
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDate

@RunWith(MockitoJUnitRunner::class)
class DatoServiceTest {
    @Test
    fun ekskludererHelgedager() {
        val dagerMellom = DatoService.dagerMellom(
            LocalDate.of(2017, 2, 17),
            LocalDate.of(2017, 2, 20)
        )
        Assertions.assertThat(dagerMellom).isEqualTo(1)
    }
}

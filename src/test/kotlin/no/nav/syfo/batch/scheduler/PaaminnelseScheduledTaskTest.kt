package no.nav.syfo.batch.scheduler

import no.nav.syfo.batch.leaderelection.LeaderElectionService
import no.nav.syfo.service.MotedeltakerService
import no.nav.syfo.util.DatoService
import no.nav.syfo.util.Toggle
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDate

@RunWith(MockitoJUnitRunner::class)
class PaaminnelseScheduledTaskTest {
    @Mock
    private lateinit var motedeltakerService: MotedeltakerService

    @Mock
    private lateinit var datoService: DatoService

    @Mock
    private lateinit var toggle: Toggle

    @Mock
    private lateinit var leaderElectionService: LeaderElectionService

    @InjectMocks
    private lateinit var paaminnelseScheduledTask: PaaminnelseScheduledTask

    @Before
    fun setup() {
        Mockito.`when`(leaderElectionService.isLeader).thenReturn(true)
        Mockito.`when`(toggle.toggleBatchPaaminelse()).thenReturn(true)
    }

    @Test
    fun senderMedNullDagerEkstraDersomVanligDag() {
        Mockito.`when`(datoService.dagensDato()).thenReturn(LocalDate.of(2016, 11, 17))
        paaminnelseScheduledTask.run()
        val argumentCaptor = ArgumentCaptor.forClass(Int::class.java)
        Mockito.verify(motedeltakerService, Mockito.times(1)).findMotedeltakereSomIkkeHarSvartSisteDognet(argumentCaptor.capture())
        Assertions.assertThat(argumentCaptor.value).isEqualTo(0)
    }

    @Test
    fun senderMedToDagerEkstraDersomMandag() {
        Mockito.`when`(datoService.dagensDato()).thenReturn(LocalDate.of(2016, 11, 21))
        paaminnelseScheduledTask.run()
        val argumentCaptor = ArgumentCaptor.forClass(Int::class.java)
        Mockito.verify(motedeltakerService, Mockito.times(1)).findMotedeltakereSomIkkeHarSvartSisteDognet(argumentCaptor.capture())
        Assertions.assertThat(argumentCaptor.value).isEqualTo(2)
    }

    @Test
    fun senderMedEnDagerEkstraDersom17Mai() {
        Mockito.`when`(datoService.dagensDato()).thenReturn(LocalDate.of(2017, 5, 18))
        paaminnelseScheduledTask.run()
        val argumentCaptor = ArgumentCaptor.forClass(Int::class.java)
        Mockito.verify(motedeltakerService, Mockito.times(1)).findMotedeltakereSomIkkeHarSvartSisteDognet(argumentCaptor.capture())
        Assertions.assertThat(argumentCaptor.value).isEqualTo(1)
    }

    @Test
    fun dagenEtterPaaske2017() {
        Mockito.`when`(datoService.dagensDato()).thenReturn(LocalDate.of(2017, 4, 18))
        paaminnelseScheduledTask.run()
        val argumentCaptor = ArgumentCaptor.forClass(Int::class.java)
        Mockito.verify(motedeltakerService, Mockito.times(1)).findMotedeltakereSomIkkeHarSvartSisteDognet(argumentCaptor.capture())
        Assertions.assertThat(argumentCaptor.value).isEqualTo(5)
    }
}

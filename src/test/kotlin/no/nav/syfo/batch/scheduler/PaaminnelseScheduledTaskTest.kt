package no.nav.syfo.batch.scheduler

import no.nav.syfo.batch.leaderelection.LeaderElectionService
import no.nav.syfo.domain.model.*
import no.nav.syfo.service.MoteService
import no.nav.syfo.service.MotedeltakerService
import no.nav.syfo.service.varselinnhold.ArbeidsgiverVarselService
import no.nav.syfo.util.DatoService
import no.nav.syfo.util.Toggle
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDate
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class PaaminnelseScheduledTaskTest {
    @Mock
    private lateinit var motedeltakerService: MotedeltakerService

    @Mock
    private lateinit var moteService: MoteService

    @Mock
    private lateinit var datoService: DatoService

    @Mock
    private lateinit var varselService: ArbeidsgiverVarselService

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

    @Test
    fun mangePaaminnelser() {
        Mockito.`when`(datoService.dagensDato()).thenReturn(LocalDate.of(2017, 4, 18))
        val moteRange = (1..1002)
        val uuider = moteRange.map { UUID.randomUUID().toString() }.toList()
        val moter = moteRange.map { i -> Mote().id(i.toLong()).uuid(uuider[i-1]) }.toList()
        val motedeltakere = moteRange.map { i -> MotedeltakerArbeidsgiver().id(i.toLong()).uuid(uuider[i-1]) }.toList()

        Mockito.`when`(motedeltakerService.findMotedeltakereSomIkkeHarSvartSisteDognet(5)).thenReturn(motedeltakere)
        moteRange.forEach {
            Mockito.`when`(moteService.findMoteByMotedeltakerUuid(uuider[it-1])).thenReturn(moter[it-1])
        }

        paaminnelseScheduledTask.run()

        Mockito.verify(motedeltakerService).findMotedeltakereSomIkkeHarSvartSisteDognet(5)
        moteRange.forEach {
            Mockito.verify(varselService).sendVarsel(Mockito.eq(Varseltype.PAAMINNELSE), Mockito.eq(moter[it-1]), Mockito.eq(true), Mockito.eq("srvmoteadmin"))
        }
    }
}

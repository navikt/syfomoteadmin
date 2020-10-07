package no.nav.syfo.service

import no.nav.syfo.domain.model.Mote
import no.nav.syfo.repository.dao.FeedDAO
import no.nav.syfo.repository.model.PFeedHendelse
import no.nav.syfo.repository.model.PFeedHendelse.FeedHendelseType
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDateTime
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class FeedServiceTest {
    @Mock
    private lateinit var feedDAO: FeedDAO

    @InjectMocks
    private lateinit var feedService: FeedService

    @Test
    fun skalOppretteHendelseHvisAlleSvarMottatt() {
        val skalOppretteFeedHendelse = feedService.skalOppretteFeedHendelse(Mote(), FeedHendelseType.ALLE_SVAR_MOTTATT)
        Assertions.assertThat(skalOppretteFeedHendelse).isTrue
    }

    @Test
    fun skalIkkeOppretteHendelseBekreftetOgIngenTidligereFeed() {
        Mockito.`when`(feedDAO.finnFeedHendelserIMote(1L)).thenReturn(emptyList())
        val skalOppretteFeedHendelse = feedService.skalOppretteFeedHendelse(Mote().id(1L), FeedHendelseType.BEKREFTET)
        Assertions.assertThat(skalOppretteFeedHendelse).isFalse
    }

    @Test
    fun skalOppretteHendelseBekreftetOgTidligereFeedErAlleSvarMottatt() {
        Mockito.`when`(feedDAO.finnFeedHendelserIMote(1L)).thenReturn(Arrays.asList(
            PFeedHendelse()
                .created(LocalDateTime.now().minusDays(7))
                .type(FeedHendelseType.ALLE_SVAR_MOTTATT.name)
        ))
        val skalOppretteFeedHendelse = feedService.skalOppretteFeedHendelse(Mote().id(1L), FeedHendelseType.BEKREFTET)
        Assertions.assertThat(skalOppretteFeedHendelse).isTrue
    }

    @Test
    fun skalIkkeOppretteHendelseBekreftetOgTidligereFeedIkkeErAlleSvarMottatt() {
        Mockito.`when`(feedDAO.finnFeedHendelserIMote(1L)).thenReturn(Arrays.asList(
            PFeedHendelse()
                .created(LocalDateTime.now().minusDays(7))
                .type(FeedHendelseType.FLERE_TIDSPUNKT.name),
            PFeedHendelse()
                .created(LocalDateTime.now().minusDays(8))
                .type(FeedHendelseType.ALLE_SVAR_MOTTATT.name)
        ))
        val skalOppretteFeedHendelse = feedService.skalOppretteFeedHendelse(Mote().id(1L), FeedHendelseType.BEKREFTET)
        Assertions.assertThat(skalOppretteFeedHendelse).isFalse
    }
}

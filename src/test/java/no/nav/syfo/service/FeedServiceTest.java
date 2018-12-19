package no.nav.syfo.service;

import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.repository.dao.FeedDAO;
import no.nav.syfo.repository.model.PFeedHendelse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static no.nav.syfo.repository.model.PFeedHendelse.FeedHendelseType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FeedServiceTest {

    @Mock
    private FeedDAO feedDAO;
    @InjectMocks
    private FeedService feedService;

    @Test
    public void skalOppretteHendelseHvisAlleSvarMottatt() {
        boolean skalOppretteFeedHendelse = feedService.skalOppretteFeedHendelse(new Mote(), ALLE_SVAR_MOTTATT);
        assertThat(skalOppretteFeedHendelse).isTrue();
    }

    @Test
    public void skalIkkeOppretteHendelseBekreftetOgIngenTidligereFeed() {
        when(feedDAO.finnFeedHendelserIMote(1L)).thenReturn(emptyList());
        boolean skalOppretteFeedHendelse = feedService.skalOppretteFeedHendelse(new Mote().id(1L), BEKREFTET);
        assertThat(skalOppretteFeedHendelse).isFalse();
    }

    @Test
    public void skalOppretteHendelseBekreftetOgTidligereFeedErAlleSvarMottatt() {
        when(feedDAO.finnFeedHendelserIMote(1L)).thenReturn(asList(
                new PFeedHendelse()
                        .created(now().minusDays(7))
                        .type(ALLE_SVAR_MOTTATT.name())
        ));
        boolean skalOppretteFeedHendelse = feedService.skalOppretteFeedHendelse(new Mote().id(1L), BEKREFTET);
        assertThat(skalOppretteFeedHendelse).isTrue();
    }

    @Test
    public void skalIkkeOppretteHendelseBekreftetOgTidligereFeedIkkeErAlleSvarMottatt() {
        when(feedDAO.finnFeedHendelserIMote(1L)).thenReturn(asList(
                new PFeedHendelse()
                        .created(now().minusDays(7))
                        .type(FLERE_TIDSPUNKT.name()),
                new PFeedHendelse()
                        .created(now().minusDays(8))
                        .type(ALLE_SVAR_MOTTATT.name())
        ));
        boolean skalOppretteFeedHendelse = feedService.skalOppretteFeedHendelse(new Mote().id(1L), BEKREFTET);
        assertThat(skalOppretteFeedHendelse).isFalse();
    }
}

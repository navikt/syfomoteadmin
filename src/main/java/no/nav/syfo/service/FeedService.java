package no.nav.syfo.service;

import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.repository.dao.FeedDAO;
import no.nav.syfo.repository.model.PFeedHendelse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static no.nav.syfo.repository.model.PFeedHendelse.FeedHendelseType.ALLE_SVAR_MOTTATT;

@Service
public class FeedService {

    private FeedDAO feedDAO;

    @Autowired
    public FeedService(FeedDAO feedDAO) {
        this.feedDAO = feedDAO;
    }

    public boolean skalOppretteFeedHendelse(Mote Mote, PFeedHendelse.FeedHendelseType feedHendelseType) {
        if (feedHendelseType.equals(ALLE_SVAR_MOTTATT)) {
            return true;
        }
        return feedDAO.finnFeedHendelserIMote(Mote.id)
                .stream()
                .sorted((o1, o2) -> o2.created.compareTo(o1.created))
                .findFirst().map(pFeedHendelse -> pFeedHendelse.type.equals(ALLE_SVAR_MOTTATT.name())).orElse(false);
    }

    public String finnNyesteFeedUuidiMote(Mote Mote) {
        return feedDAO.finnFeedHendelserIMote(Mote.id)
                .stream()
                .sorted((o1, o2) -> o2.created.compareTo(o1.created))
                .findFirst().map(pFeedHendelse -> pFeedHendelse.uuid).orElseThrow(() -> new RuntimeException("Vi fant ikke foreldren til denne. Dette burde ikke skje! MoteId " + Mote.id));
    }
}

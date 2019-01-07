package no.nav.syfo.rest.api.system;

import no.nav.dialogarena.aktor.AktorService;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.repository.dao.FeedDAO;
import no.nav.syfo.repository.dao.MoteDAO;
import no.nav.syfo.rest.api.system.domain.VeilederOppgaveFeedItem;
import no.nav.syfo.service.exceptions.MoteException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.List;

import static java.time.LocalDateTime.parse;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.syfo.util.RestUtils.baseUrl;

@Component
@Path("/system/feed/moter")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class MoterFeed {

    @Inject
    private FeedDAO feedDAO;
    @Inject
    private MoteDAO moteDAO;
    @Inject
    private AktorService aktorService;

    @GET
    public List<VeilederOppgaveFeedItem> moterFeed(@QueryParam("timestamp") String timestamp) {
        return feedDAO.hendelserEtterTidspunkt(parse(timestamp)).stream()
                .map(feedHendelse -> {
                            Mote mote = moteDAO.findMoteByID(feedHendelse.moteId);
                            String fnr = aktorService.getFnr(mote.sykmeldt().aktorId).orElseThrow(() -> new MoteException("FNR ikke funnet for aktoerId!"));
                            return new VeilederOppgaveFeedItem()
                                    .uuid(feedHendelse.uuid)
                                    .tildeltEnhet(mote.navEnhet)
                                    .tildeltIdent(mote.eier)
                                    .status(finnStatus(feedHendelse.type))
                                    .fnr(fnr)
                                    .lenke(baseUrl() + "/sykefravaer/" + fnr + "/mote")
                                    .type(feedHendelse.type)
                                    .sistEndretAv(feedHendelse.sistEndretAv)
                                    .created(feedHendelse.created)
                                    .status(feedHendelse.status())
                                    .virksomhetsnummer(mote.arbeidsgiver().orgnummer);
                        }
                ).collect(toList());
    }

    private String finnStatus(String type) {
        if ("ALLE_SVAR_MOTTTATT".equals(type)) {
            return "IKKE_STARTET";
        }
        return "FERDIG";
    }
}

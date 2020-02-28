package no.nav.syfo.api.system;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.api.Unprotected;
import no.nav.syfo.api.system.domain.VeilederOppgaveFeedItem;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.repository.dao.FeedDAO;
import no.nav.syfo.repository.dao.MoteDAO;
import no.nav.syfo.service.AktoerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

import static java.time.LocalDateTime.parse;
import static java.util.stream.Collectors.toList;
import static no.nav.syfo.oidc.OIDCIssuer.INTERN;
import static no.nav.syfo.util.RestUtils.baseUrl;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@ProtectedWithClaims(issuer = INTERN, claimMap = {"sub=srvsyfoveilederoppgaver"})
@RequestMapping(value = "/api/system/feed/moter")
public class MoterFeed {

    private FeedDAO feedDAO;

    private MoteDAO moteDAO;

    private AktoerService aktorService;

    @Inject
    public MoterFeed(
            FeedDAO feedDAO,
            MoteDAO moteDAO,
            AktoerService aktorService
    ) {
        this.feedDAO = feedDAO;
        this.moteDAO = moteDAO;
        this.aktorService = aktorService;
    }

    @Unprotected
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<VeilederOppgaveFeedItem> moterFeed(@RequestParam(value = "timestamp") String timestamp) {
        return feedDAO.hendelserEtterTidspunkt(parse(timestamp)).stream()
                .map(feedHendelse -> {
                            Mote mote = moteDAO.findMoteByID(feedHendelse.moteId);
                            String fnr = aktorService.hentFnrForAktoer(mote.sykmeldt().aktorId);
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

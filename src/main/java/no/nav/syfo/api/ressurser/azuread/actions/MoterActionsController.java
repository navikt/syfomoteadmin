package no.nav.syfo.api.ressurser.azuread.actions;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.api.domain.RSOverforMoter;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.MoteService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.api.auth.OIDCIssuer.AZURE;
import static no.nav.syfo.api.auth.OIDCUtil.getSubjectInternAzure;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@ProtectedWithClaims(issuer = AZURE)
@RequestMapping(value = "/api/internad/actions/moter")
public class MoterActionsController {

    private OIDCRequestContextHolder contextHolder;
    private Metrikk metrikk;
    private MoteService moteService;

    @Inject
    public MoterActionsController(
            OIDCRequestContextHolder contextHolder,
            Metrikk metrikk,
            MoteService moteService
    ) {
        this.contextHolder = contextHolder;
        this.metrikk = metrikk;
        this.moteService = moteService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/overfor")
    public void transferDialogmoter(@RequestBody RSOverforMoter rsOverforMoter) {
        String innloggetIdent = getSubjectInternAzure(contextHolder);
        rsOverforMoter.moteUuidListe.forEach(moteUuid -> moteService.overforMoteTil(moteUuid, innloggetIdent));

        metrikk.tellEndepunktKall("overfor_mote");
    }
}

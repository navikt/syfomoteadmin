package no.nav.syfo.api.ressurser.azuread.v2;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.api.domain.RSOverforMoter;
import no.nav.syfo.metric.Metric;
import no.nav.syfo.service.MoteService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.api.auth.OIDCIssuer.VEILEDER_AZURE_V2;
import static no.nav.syfo.api.auth.OIDCUtil.getSubjectInternAzureV2;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@ProtectedWithClaims(issuer = VEILEDER_AZURE_V2)
@RequestMapping(value = "/api/internad/v2/actions/moter")
public class MoterActionsControllerV2 {

    private final OIDCRequestContextHolder contextHolder;
    private final Metric metric;
    private final MoteService moteService;

    @Inject
    public MoterActionsControllerV2(
            OIDCRequestContextHolder contextHolder,
            Metric metric,
            MoteService moteService
    ) {
        this.contextHolder = contextHolder;
        this.metric = metric;
        this.moteService = moteService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/overfor")
    public void transferDialogmoter(@RequestBody RSOverforMoter rsOverforMoter) {
        String innloggetIdent = getSubjectInternAzureV2(contextHolder);
        rsOverforMoter.moteUuidListe.forEach(
                moteUuid -> moteService.overforMoteTil(moteUuid, innloggetIdent)
        );
        metric.tellEndepunktKall("overfor_mote");
    }
}

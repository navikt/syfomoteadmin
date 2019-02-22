package no.nav.syfo.api.ressurser.actions;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.api.domain.RSOverforMoter;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.MoteService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

import static no.nav.syfo.util.OIDCUtil.getSubjectIntern;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/actions/moter")
public class MoterActions {

    private OIDCRequestContextHolder contextHolder;
    private Metrikk metrikk;
    private MoteService moteService;

    @Inject
    public MoterActions(
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
    public void overforMoter(@RequestBody RSOverforMoter rsOverforMoter) {
        rsOverforMoter.moteUuidListe.forEach(moteUuid -> moteService.overforMoteTil(moteUuid, getSubjectIntern(contextHolder)));

        metrikk.tellEndepunktKall("overfor_mote");
    }
}

package no.nav.syfo.api.ressurser;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.consumer.pdl.PdlConsumer;
import no.nav.syfo.domain.AktorId;
import no.nav.syfo.domain.Fodselsnummer;
import no.nav.syfo.api.domain.bruker.*;
import no.nav.syfo.metric.Metric;
import no.nav.syfo.service.*;
import no.nav.syfo.util.Brukerkontekst;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

import static no.nav.syfo.api.auth.OIDCIssuer.EKSTERN;
import static no.nav.syfo.api.auth.OIDCUtil.getSubjectEkstern;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/bruker")
@ProtectedWithClaims(issuer = EKSTERN)
public class BrukerMoterRessurs {

    private OIDCRequestContextHolder contextHolder;

    private final Metric metric;

    private BrukertilgangService brukertilgangService;

    private MoteBrukerService moteBrukerService;

    private final PdlConsumer pdlConsumer;

    @Inject
    public BrukerMoterRessurs(
            OIDCRequestContextHolder contextHolder,
            Metric metric,
            BrukertilgangService brukertilgangService,
            MoteBrukerService moteBrukerService,
            PdlConsumer pdlConsumer
    ) {
        this.contextHolder = contextHolder;
        this.metric = metric;
        this.pdlConsumer = pdlConsumer;
        this.brukertilgangService = brukertilgangService;
        this.moteBrukerService = moteBrukerService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/arbeidsgiver/moter")
    public List<BrukerMote> hentMoter() {
        String innloggetIdent = getSubjectEkstern(contextHolder);
        AktorId innloggetAktorId = pdlConsumer.aktorId(new Fodselsnummer(innloggetIdent));

        metric.tellEndepunktKall("hent_mote_arbeidsgiver");

        return moteBrukerService.hentBrukerMoteListe(innloggetAktorId.getValue(), Brukerkontekst.ARBEIDSGIVER);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/arbeidstaker/moter/siste")
    public BrukerMote hentSisteMote() {
        String innloggetIdent = getSubjectEkstern(contextHolder);
        AktorId innloggetAktorId = pdlConsumer.aktorId(new Fodselsnummer(innloggetIdent));

        brukertilgangService.kastExceptionHvisIkkeTilgang(innloggetIdent);

        metric.tellEndepunktKall("hent_mote_arbeidstaker");

        return moteBrukerService.hentSisteBrukerMote(innloggetAktorId.getValue(), Brukerkontekst.ARBEIDSTAKER);
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/moter/{moteUuid}/send")
    public BrukerOppdaterMoteSvar oppdaterMotedeltaker(
            @PathVariable("moteUuid") final String moteUuid,
            @RequestBody BrukerMoteSvar motesvar
    ) {
        tellMoteSvar(motesvar);
        return moteBrukerService.sendSvar(moteUuid, motesvar);
    }

    private void tellMoteSvar(BrukerMoteSvar motesvar) {
        if (Brukerkontekst.ARBEIDSTAKER.equals(motesvar.deltakertype)) {
            metric.tellEndepunktKall("svar_mote_arbeidstaker");
        } else {
            metric.tellEndepunktKall("svar_mote_arbeidsgiver");
        }
    }
}

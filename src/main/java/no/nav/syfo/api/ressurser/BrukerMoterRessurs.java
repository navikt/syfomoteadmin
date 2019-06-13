package no.nav.syfo.api.ressurser;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.api.domain.bruker.*;
import no.nav.syfo.service.*;
import no.nav.syfo.util.Brukerkontekst;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.util.OIDCUtil.getSubjectEkstern;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/bruker")
@ProtectedWithClaims(issuer = EKSTERN)
@Slf4j
public class BrukerMoterRessurs {

    private OIDCRequestContextHolder contextHolder;

    private AktoerService aktoerService;

    private BrukertilgangService brukertilgangService;

    private MoteBrukerService moteBrukerService;

    @Inject
    public BrukerMoterRessurs(
            OIDCRequestContextHolder contextHolder,
            AktoerService aktoerService,
            BrukertilgangService brukertilgangService,
            MoteBrukerService moteBrukerService
    ) {
        this.contextHolder = contextHolder;
        this.aktoerService = aktoerService;
        this.brukertilgangService = brukertilgangService;
        this.moteBrukerService = moteBrukerService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/arbeidsgiver/moter")
    public List<BrukerMote> hentMoter() {
        String innloggetIdent = getSubjectEkstern(contextHolder);
        String innloggetAktorId = aktoerService.hentAktoerIdForIdent(innloggetIdent);

        return moteBrukerService.hentBrukerMoteListe(innloggetAktorId, Brukerkontekst.ARBEIDSGIVER);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/arbeidstaker/moter/siste")
    public BrukerMote hentSisteMote() {
        String innloggetIdent = getSubjectEkstern(contextHolder);
        String innloggetAktorId = aktoerService.hentAktoerIdForIdent(innloggetIdent);

        brukertilgangService.kastExceptionHvisIkkeTilgang(innloggetIdent);

        return moteBrukerService.hentSisteBrukerMote(innloggetAktorId, Brukerkontekst.ARBEIDSTAKER);
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/moter/{moteUuid}/send")
    public BrukerOppdaterMoteSvar oppdaterMotedeltaker(
            @PathVariable("moteUuid") final String moteUuid,
            @RequestBody BrukerMoteSvar motesvar
    ) {
        return moteBrukerService.sendSvar(moteUuid, motesvar);
    }
}

package no.nav.syfo.api.ressurser;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.api.domain.bruker.BrukerMote;
import no.nav.syfo.api.domain.bruker.BrukerMoteSvar;
import no.nav.syfo.api.domain.bruker.BrukerOppdaterMoteSvar;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.BrukertilgangService;
import no.nav.syfo.service.MoteBrukerService;
import no.nav.syfo.util.Brukerkontekst;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.util.List;

import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.util.OIDCUtil.getSubjectEkstern;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/bruker")
@ProtectedWithClaims(issuer = EKSTERN)
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

    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    void handleBadRequests(HttpServletResponse response) throws IOException {
        response.sendError(BAD_REQUEST.value(), "Vi kunne ikke tolke inndataene :/");
    }

    @ExceptionHandler({NotFoundException.class})
    void handleNotFoundRequests(HttpServletResponse response) throws IOException {
        response.sendError(NOT_FOUND.value(), "Fant ikke møte");
    }

    @ExceptionHandler({ForbiddenException.class})
    void handleForbiddenRequests(HttpServletResponse response) throws IOException {
        response.sendError(FORBIDDEN.value(), "Handling er forbudt");
    }
}

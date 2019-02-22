package no.nav.syfo.api.ressurser;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.api.domain.RSVeilederInfo;
import no.nav.syfo.service.VeilederService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ForbiddenException;
import java.io.IOException;

import static no.nav.syfo.oidc.OIDCIssuer.INTERN;
import static no.nav.syfo.util.OIDCUtil.getSubjectIntern;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/veilederinfo")
@ProtectedWithClaims(issuer = INTERN)
public class VeilederRessurs {

    private OIDCRequestContextHolder contextHolder;

    private VeilederService veilederService;

    @Inject
    public VeilederRessurs(
            OIDCRequestContextHolder contextHolder,
            VeilederService veilederService
    ) {
        this.contextHolder = contextHolder;
        this.veilederService = veilederService;
    }

    @GetMapping
    public RSVeilederInfo hentNavn() {
        return hentIdent(getSubjectIntern(contextHolder));
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/{ident}")
    public RSVeilederInfo hentIdent(@PathVariable("ident") String ident) {
        return new RSVeilederInfo()
                .navn(veilederService.hentVeileder(ident).navn)
                .ident(ident);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    void handleBadRequests(HttpServletResponse response) throws IOException {
        response.sendError(BAD_REQUEST.value(), "Vi kunne ikke tolke inndataene :/");
    }

    @ExceptionHandler({ForbiddenException.class})
    void handleForbiddenRequests(HttpServletResponse response) throws IOException {
        response.sendError(FORBIDDEN.value(), "Handling er forbudt");
    }
}

package no.nav.syfo.api.ressurser;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.api.domain.RSEnheter;
import no.nav.syfo.service.NorgService;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ForbiddenException;
import java.io.IOException;

import static no.nav.syfo.api.mappers.RSEnhetMapper.enhet2rs;
import static no.nav.syfo.oidc.OIDCIssuer.INTERN;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.OIDCUtil.getSubjectIntern;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/enheter")
@ProtectedWithClaims(issuer = INTERN)
public class NorgRessurs {

    private OIDCRequestContextHolder contextHolder;

    private NorgService norgService;

    @Inject
    public NorgRessurs(
            OIDCRequestContextHolder contextHolder,
            NorgService norgService
    ) {
        this.contextHolder = contextHolder;
        this.norgService = norgService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public RSEnheter hentEnheter() {
        return new RSEnheter()
                .enhetliste(mapListe(norgService.hentVeiledersNavEnheter(getSubjectIntern(contextHolder)), enhet2rs));
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

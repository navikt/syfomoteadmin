package no.nav.syfo.api.ressurser;

import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.api.domain.RSAktor;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.TilgangService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.oidc.OIDCIssuer.INTERN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/aktor/{aktorId}")
@ProtectedWithClaims(issuer = INTERN)
public class AktoerRessurs {

    private AktoerService aktoerService;

    private TilgangService tilgangService;

    @Inject
    public AktoerRessurs(
            AktoerService aktoerService,
            TilgangService tilgangService
    ) {
        this.aktoerService = aktoerService;
        this.tilgangService = tilgangService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public RSAktor get(@PathVariable("aktorId") String aktorId) {
        final String fnr = aktoerService.hentFnrForAktoer(aktorId);

        tilgangService.kastExceptionHvisIkkeVeilederHarTilgangTilPerson(fnr);

        return new RSAktor().fnr(fnr);
    }
}

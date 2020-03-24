package no.nav.syfo.api.ressurser.azuread;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.aktorregister.domain.AktorId;
import no.nav.syfo.api.domain.RSAktor;
import no.nav.syfo.domain.Fnr;
import no.nav.syfo.service.TilgangService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.oidc.OIDCIssuer.AZURE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/internad/aktor/{aktorId}")
@ProtectedWithClaims(issuer = AZURE)
public class AktorController {

    private final AktorregisterConsumer aktorregisterConsumer;
    private final TilgangService tilgangService;

    @Inject
    public AktorController(
            AktorregisterConsumer aktorregisterConsumer,
            TilgangService tilgangService
    ) {
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.tilgangService = tilgangService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public RSAktor get(@PathVariable("aktorId") String aktorId) {
        final Fnr fnr = Fnr.of(aktorregisterConsumer.getFnrForAktorId(new AktorId(aktorId)));

        tilgangService.throwExceptionIfVeilederWithoutAccess(fnr);

        return new RSAktor().fnr(fnr.getFnr());
    }
}

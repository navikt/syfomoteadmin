package no.nav.syfo.api.ressurser.azuread;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.syfo.api.domain.RSAktor;
import no.nav.syfo.consumer.pdl.PdlConsumer;
import no.nav.syfo.consumer.veiledertilgang.VeilederTilgangConsumer;
import no.nav.syfo.domain.AktorId;
import no.nav.syfo.domain.Fodselsnummer;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.api.auth.OIDCIssuer.AZURE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/internad/aktor/{aktorId}")
@ProtectedWithClaims(issuer = AZURE)
public class AktorController {

    private final PdlConsumer pdlConsumer;
    private final VeilederTilgangConsumer tilgangService;

    @Inject
    public AktorController(
            PdlConsumer pdlConsumer,
            VeilederTilgangConsumer tilgangService
    ) {
        this.pdlConsumer = pdlConsumer;
        this.tilgangService = tilgangService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public RSAktor get(@PathVariable("aktorId") String aktorId) {
        final Fodselsnummer fnr = pdlConsumer.fodselsnummer(new AktorId(aktorId));

        tilgangService.throwExceptionIfVeilederWithoutAccess(fnr);

        return new RSAktor().fnr(fnr.getValue());
    }
}

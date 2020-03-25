package no.nav.syfo.api.ressurser.azuread;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.syfo.api.domain.RSVirksomhet;
import no.nav.syfo.ereg.EregConsumer;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.oidc.OIDCIssuer.AZURE;
import static org.apache.commons.lang3.text.WordUtils.capitalize;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/internad/virksomhet/{orgnummer}")
@ProtectedWithClaims(issuer = AZURE)
public class VirksomhetController {

    private final EregConsumer eregConsumer;

    @Inject
    public VirksomhetController(
            EregConsumer eregConsumer
    ) {
        this.eregConsumer = eregConsumer;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public RSVirksomhet getVirksomhetsnavn(@PathVariable("orgnummer") String orgnummer) {
        return new RSVirksomhet()
                .navn(capitalize(eregConsumer.virksomhetsnavn(orgnummer).toLowerCase()));
    }
}

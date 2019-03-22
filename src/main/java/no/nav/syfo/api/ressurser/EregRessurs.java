package no.nav.syfo.api.ressurser;

import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.api.domain.RSVirksomhet;
import no.nav.syfo.service.OrganisasjonService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.oidc.OIDCIssuer.INTERN;
import static org.apache.commons.lang3.text.WordUtils.capitalize;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/virksomhet/{orgnummer}")
@ProtectedWithClaims(issuer = INTERN)
public class EregRessurs {

    private OrganisasjonService organisasjonService;

    @Inject
    public EregRessurs(
            OrganisasjonService organisasjonService
    ) {
        this.organisasjonService = organisasjonService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public RSVirksomhet hentOrganisasjonsNavn(@PathVariable("orgnummer") String orgnummer) {
        return new RSVirksomhet().navn(capitalize(organisasjonService.hentNavn(orgnummer).toLowerCase()));
    }
}

package no.nav.syfo.api.ressurser;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.syfo.service.MoteBrukerService;
import no.nav.syfo.util.Brukerkontekst;
import org.springframework.web.bind.annotation.*;
import javax.inject.Inject;
import java.time.LocalDateTime;

import static no.nav.syfo.oidc.OIDCIssuer.STS;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/system")
public class SystemMoterRessurs {

    private MoteBrukerService moteBrukerService;

    @Inject
    public SystemMoterRessurs(
            MoteBrukerService moteBrukerService
    ) {
        this.moteBrukerService = moteBrukerService;
    }

    @PostMapping(produces = APPLICATION_JSON_VALUE, value = "/{aktorId}/harAktivtMote")
    @ProtectedWithClaims(issuer = STS)
    public boolean hentOmMoteErOpprettetEtterDato(
            @PathVariable("aktorId") String aktorId,
            @RequestBody LocalDateTime dato) {
        return moteBrukerService.hentSisteBrukerMoteEtterDato(aktorId, Brukerkontekst.ARBEIDSTAKER, dato).isPresent();
    }
}

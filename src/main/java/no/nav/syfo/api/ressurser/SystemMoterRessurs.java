package no.nav.syfo.api.ressurser;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.service.MoteBrukerService;
import no.nav.syfo.util.Brukerkontekst;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.oidc.OIDCIssuer.INTERN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/system")
@Slf4j
public class SystemMoterRessurs {

    private MoteBrukerService moteBrukerService;

    @Inject
    public SystemMoterRessurs(
            MoteBrukerService moteBrukerService
    ) {
        this.moteBrukerService = moteBrukerService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, value = "/{aktorId}/harAktivtMote")
    @ProtectedWithClaims(issuer = INTERN)
    public boolean hentOmBrukerHarMoteINyesteOppfolgingstilfelle(@PathVariable("aktorId") String aktorId) {
        return moteBrukerService.hentSisteBrukerMoteINyesteOppfolgingstilfelle(aktorId, Brukerkontekst.ARBEIDSTAKER) != null;
    }
}

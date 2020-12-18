package no.nav.syfo.api.ressurser;

import no.nav.security.oidc.api.Protected;
import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.syfo.domain.Fodselsnummer;
import no.nav.syfo.service.MoteBrukerService;
import no.nav.syfo.domain.Brukerkontekst;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.time.LocalDateTime;

import static no.nav.syfo.api.auth.OIDCIssuer.STS;
import static no.nav.syfo.util.RequestUtilKt.NAV_PERSONIDENTER_HEADER;
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
    @Protected
    public boolean hentOmMoteErOpprettetEtterDato(
            @PathVariable("aktorId") String aktorId,
            @RequestBody LocalDateTime dato) {
        return moteBrukerService.hentSisteBrukerMoteEtterDato(aktorId, Brukerkontekst.ARBEIDSTAKER, dato).isPresent();
    }

    @PostMapping(produces = APPLICATION_JSON_VALUE, value = "/moteplanlegger/aktiv")
    @ProtectedWithClaims(issuer = STS)
    public boolean harAktivMoteplanleggerOpprettetEtterDato(
            @RequestHeader(NAV_PERSONIDENTER_HEADER) String ident,
            @RequestBody LocalDateTime tidligsteOpprettetGrense
    ) {
        Fodselsnummer arbeidstakerFnr = new Fodselsnummer(ident);
        return moteBrukerService.harMoteplanleggerIBruk(arbeidstakerFnr, Brukerkontekst.ARBEIDSTAKER, tidligsteOpprettetGrense);
    }
}

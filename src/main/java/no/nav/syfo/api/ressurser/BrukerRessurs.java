package no.nav.syfo.api.ressurser;

import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.api.domain.RSBruker;
import no.nav.syfo.api.domain.RSReservasjon;
import no.nav.syfo.domain.model.Kontaktinfo;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.BrukerprofilService;
import no.nav.syfo.service.DkifService;
import no.nav.syfo.service.TilgangService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ForbiddenException;
import java.io.IOException;

import static no.nav.syfo.domain.model.Kontaktinfo.FeilAarsak.*;
import static no.nav.syfo.oidc.OIDCIssuer.INTERN;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/brukerinfo/{ident}")
@ProtectedWithClaims(issuer = INTERN)
public class BrukerRessurs {

    private BrukerprofilService brukerprofilService;
    private DkifService dkifService;
    private AktoerService aktoerService;
    private TilgangService tilgangService;

    @Inject
    public BrukerRessurs(
            DkifService dkifService,
            AktoerService aktoerService,
            BrukerprofilService brukerprofilService,
            TilgangService tilgangService
    ) {
        this.dkifService = dkifService;
        this.aktoerService = aktoerService;
        this.brukerprofilService = brukerprofilService;
        this.tilgangService = tilgangService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/api/navn")
    public RSBruker hentBruker(@PathVariable("ident") String ident) {
        String fnr;
        if (ident.matches("\\d{11}$")) {
            fnr = ident;
        } else {
            fnr = aktoerService.hentFnrForAktoer(ident);
        }

        tilgangService.kastExceptionHvisIkkeVeilederHarTilgangTilPerson(fnr);

        return new RSBruker().navn(brukerprofilService.hentBruker(fnr).navn);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public RSBruker bruker(@PathVariable("ident") String ident) {
        String fnr;
        if (ident.matches("\\d{11}$")) {
            fnr = ident;
        } else {
            fnr = aktoerService.hentFnrForAktoer(ident);
        }
        tilgangService.kastExceptionHvisIkkeVeilederHarTilgangTilPerson(fnr);

        RSBruker rsBruker = new RSBruker();
        Kontaktinfo kontaktinfo = dkifService.hentKontaktinfoFnr(fnr);
        rsBruker.kontaktinfo
                .tlf(kontaktinfo.tlf)
                .epost(kontaktinfo.epost)
                .reservasjon(rsBruker.kontaktinfo.reservasjon
                        .skalHaVarsel(kontaktinfo.skalHaVarsel)
                        .feilAarsak(!kontaktinfo.skalHaVarsel ? feilAarsak(kontaktinfo.feilAarsak) : null));
        return rsBruker
                .navn(brukerprofilService.hentBruker(fnr).navn);
    }

    private RSReservasjon.KontaktInfoFeilAarsak feilAarsak(Kontaktinfo.FeilAarsak feilAarsak) {
        if (feilAarsak == SIKKERHETSBEGRENSNING) {
            return RSReservasjon.KontaktInfoFeilAarsak.KODE6;
        } else if (feilAarsak == KONTAKTINFO_IKKE_FUNNET || feilAarsak == PERSON_IKKE_FUNNET) {
            return RSReservasjon.KontaktInfoFeilAarsak.INGEN_KONTAKTINFORMASJON;
        } else if (feilAarsak == Kontaktinfo.FeilAarsak.RESERVERT) {
            return RSReservasjon.KontaktInfoFeilAarsak.RESERVERT;
        } else if (feilAarsak == Kontaktinfo.FeilAarsak.UTGAATT) {
            return RSReservasjon.KontaktInfoFeilAarsak.UTGAATT;
        } else {
            throw new RuntimeException("Fant ikke feilårsak. Sjekk mappingen");
        }
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

package no.nav.syfo.api.ressurser.azuread;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.syfo.api.domain.RSBruker;
import no.nav.syfo.api.domain.RSReservasjon;
import no.nav.syfo.domain.Fnr;
import no.nav.syfo.domain.model.Kontaktinfo;
import no.nav.syfo.service.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.domain.model.Kontaktinfo.FeilAarsak.*;
import static no.nav.syfo.oidc.OIDCIssuer.AZURE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/internad/brukerinfo/{ident}")
@ProtectedWithClaims(issuer = AZURE)
public class PersonController {

    private BrukerprofilService brukerprofilService;
    private DkifService dkifService;
    private AktoerService aktoerService;
    private TilgangService tilgangService;

    @Inject
    public PersonController(
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
    @RequestMapping(value = "/navn")
    public RSBruker hentBruker(@PathVariable("ident") String ident) {
        Fnr fnr = getFnrForIdent(ident);

        tilgangService.throwExceptionIfVeilederWithoutAccess(fnr);

        return new RSBruker()
                .navn(brukerprofilService.hentBruker(fnr.getFnr()).navn);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public RSBruker bruker(@PathVariable("ident") String ident) {
        Fnr fnr = getFnrForIdent(ident);

        tilgangService.throwExceptionIfVeilederWithoutAccess(fnr);

        RSBruker rsBruker = new RSBruker();
        Kontaktinfo kontaktinfo = dkifService.hentKontaktinfoFnr(fnr.getFnr(), AZURE);
        rsBruker.kontaktinfo
                .tlf(kontaktinfo.tlf)
                .epost(kontaktinfo.epost)
                .reservasjon(rsBruker.kontaktinfo.reservasjon
                        .skalHaVarsel(kontaktinfo.skalHaVarsel)
                        .feilAarsak(!kontaktinfo.skalHaVarsel ? feilAarsak(kontaktinfo.feilAarsak) : null));
        return rsBruker
                .navn(brukerprofilService.hentBruker(fnr.getFnr()).navn);
    }

    private Fnr getFnrForIdent(@PathVariable("ident") String ident) {
        Fnr fnr;
        if (ident.matches("\\d{11}$")) {
            fnr = Fnr.of(ident);
        } else {
            fnr = Fnr.of(aktoerService.hentFnrForAktoer(ident));
        }
        return fnr;
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
}

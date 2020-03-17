package no.nav.syfo.api.ressurser.azuread;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.syfo.api.domain.RSBruker;
import no.nav.syfo.dkif.DigitalKontaktinfo;
import no.nav.syfo.dkif.DkifConsumer;
import no.nav.syfo.domain.Fnr;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.TilgangService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.oidc.OIDCIssuer.AZURE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/internad/brukerinfo/{ident}")
@ProtectedWithClaims(issuer = AZURE)
public class PersonController {

    private DkifConsumer dkifConsumer;
    private AktoerService aktoerService;
    private PdlConsumer pdlConsumer;
    private TilgangService tilgangService;
    private Metrikk metrikk;

    @Inject
    public PersonController(
            DkifConsumer dkifConsumer,
            AktoerService aktoerService,
            PdlConsumer pdlConsumer,
            TilgangService tilgangService,
            Metrikk metrikk
    ) {
        this.dkifConsumer = dkifConsumer;
        this.aktoerService = aktoerService;
        this.pdlConsumer = pdlConsumer;
        this.tilgangService = tilgangService;
        this.metrikk = metrikk;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/navn")
    public RSBruker hentBruker(@PathVariable("ident") String ident) {
        metrikk.tellEndepunktKall("bruker_navn");

        Fnr fnr = getFnrForIdent(ident);

        tilgangService.throwExceptionIfVeilederWithoutAccess(fnr);

        return new RSBruker()
                .navn(pdlConsumer.fullName(fnr.getFnr()));
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public RSBruker bruker(@PathVariable("ident") String ident) {
        metrikk.tellEndepunktKall("bruker");

        Fnr fnr = getFnrForIdent(ident);

        tilgangService.throwExceptionIfVeilederWithoutAccess(fnr);

        RSBruker rsBruker = new RSBruker();
        DigitalKontaktinfo kontaktinfo = dkifConsumer.kontaktinformasjon(fnr.getFnr());
        rsBruker.kontaktinfo
                .tlf(kontaktinfo.getMobiltelefonnummer())
                .epost(kontaktinfo.getEpostadresse())
                .reservasjon(rsBruker.kontaktinfo.reservasjon
                        .skalHaVarsel(kontaktinfo.getKanVarsles())
                        .feilAarsak(null));
        return rsBruker
                .navn(pdlConsumer.fullName(fnr.getFnr()));
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
}

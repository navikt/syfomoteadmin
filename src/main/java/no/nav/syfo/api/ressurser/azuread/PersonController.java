package no.nav.syfo.api.ressurser.azuread;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.syfo.consumer.aktorregister.AktorregisterConsumer;
import no.nav.syfo.consumer.aktorregister.domain.*;
import no.nav.syfo.api.domain.RSBruker;
import no.nav.syfo.consumer.dkif.DigitalKontaktinfo;
import no.nav.syfo.consumer.dkif.DkifConsumer;
import no.nav.syfo.consumer.veiledertilgang.VeilederTilgangConsumer;
import no.nav.syfo.metric.Metric;
import no.nav.syfo.consumer.pdl.PdlConsumer;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.api.auth.OIDCIssuer.AZURE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/internad/brukerinfo/{ident}")
@ProtectedWithClaims(issuer = AZURE)
public class PersonController {

    private AktorregisterConsumer aktorregisterConsumer;
    private DkifConsumer dkifConsumer;
    private PdlConsumer pdlConsumer;
    private VeilederTilgangConsumer tilgangService;
    private Metric metric;

    @Inject
    public PersonController(
            AktorregisterConsumer aktorregisterConsumer,
            DkifConsumer dkifConsumer,
            PdlConsumer pdlConsumer,
            VeilederTilgangConsumer tilgangService,
            Metric metric
    ) {
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.dkifConsumer = dkifConsumer;
        this.pdlConsumer = pdlConsumer;
        this.tilgangService = tilgangService;
        this.metric = metric;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/navn")
    public RSBruker hentBruker(@PathVariable("ident") String ident) {
        metric.tellEndepunktKall("bruker_navn");

        Fodselsnummer fnr = getFnrForIdent(ident);

        tilgangService.throwExceptionIfVeilederWithoutAccess(fnr);

        return new RSBruker()
                .navn(pdlConsumer.fullName(fnr.getValue()));
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public RSBruker bruker(@PathVariable("ident") String ident) {
        metric.tellEndepunktKall("bruker");

        Fodselsnummer fnr = getFnrForIdent(ident);

        tilgangService.throwExceptionIfVeilederWithoutAccess(fnr);

        RSBruker rsBruker = new RSBruker();
        DigitalKontaktinfo kontaktinfo = dkifConsumer.kontaktinformasjon(fnr.getValue());
        rsBruker.kontaktinfo
                .tlf(kontaktinfo.getMobiltelefonnummer())
                .epost(kontaktinfo.getEpostadresse())
                .reservasjon(rsBruker.kontaktinfo.reservasjon
                        .skalHaVarsel(kontaktinfo.getKanVarsles())
                        .feilAarsak(null));
        return rsBruker
                .navn(pdlConsumer.fullName(fnr.getValue()));
    }

    private Fodselsnummer getFnrForIdent(@PathVariable("ident") String ident) {
        Fodselsnummer fnr;
        if (ident.matches("\\d{11}$")) {
            fnr = new Fodselsnummer(ident);
        } else {
            fnr = new Fodselsnummer(aktorregisterConsumer.getFnrForAktorId(new AktorId(ident)));
        }
        return fnr;
    }
}

package no.nav.syfo.api.ressurser.azuread;

import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.api.domain.RSBrukerPaaEnhet;
import no.nav.syfo.service.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.nav.syfo.oidc.OIDCIssuer.AZURE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static no.nav.syfo.api.domain.RSBrukerPaaEnhet.Skjermingskode.*;

@RestController
@RequestMapping(value = "/api/enhet")
@ProtectedWithClaims(issuer = AZURE)
public class EnhetRessurs {

    private MotedeltakerService motedeltakerService;

    private AktoerService aktoerService;

    private BrukerprofilService brukerprofilService;

    private EgenAnsattService egenAnsattService;

    private TilgangService tilgangService;

    @Inject
    public EnhetRessurs(
            MotedeltakerService motedeltakerService,
            AktoerService aktoerService,
            BrukerprofilService brukerprofilService,
            EgenAnsattService egenAnsattService,
            TilgangService tilgangService
    ) {
        this.motedeltakerService = motedeltakerService;
        this.aktoerService = aktoerService;
        this.brukerprofilService = brukerprofilService;
        this.egenAnsattService = egenAnsattService;
        this.tilgangService = tilgangService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/{enhet}/moter/brukere")
    public List<RSBrukerPaaEnhet> hentSykmeldteMedAktiveMoterForEnhet(@PathVariable("enhet") String enhet) {
        tilgangService.kastExceptionHvisIkkeVeilederHarTilgangTilEnhet(enhet);

        return motedeltakerService.sykmeldteMedMoteHvorBeggeHarSvart(enhet)
                .stream()
                .map(motedeltakerAktorId -> aktoerService.hentFnrForAktoer(motedeltakerAktorId))
                .filter(sykmeldtFnr -> tilgangService.harVeilederTilgangTilPersonViaAzure(sykmeldtFnr))
                .map(sykmeldtFnr -> new RSBrukerPaaEnhet()
                        .fnr(sykmeldtFnr)
                        .skjermingskode(hentBrukersSkjermingskode(sykmeldtFnr)))
                .collect(toList());
    }

    private RSBrukerPaaEnhet.Skjermingskode hentBrukersSkjermingskode(String fnr) {
        if (brukerprofilService.hentBruker(fnr).skjermetBruker())
            return DISKRESJONSMERKET;
        if (egenAnsattService.erEgenAnsatt(fnr))
            return EGEN_ANSATT;
        return INGEN;
    }
}
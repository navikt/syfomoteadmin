package no.nav.syfo.api.ressurser.azuread;

import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.api.domain.RSHistorikk;
import no.nav.syfo.domain.Fnr;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.service.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static no.nav.syfo.oidc.OIDCIssuer.AZURE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@ProtectedWithClaims(issuer = AZURE)
@RequestMapping(value = "/api/internad/historikk")
public class HistoryController {

    private TilgangService tilgangService;

    private AktoerService aktoerService;

    private MoteService moteService;

    private HistorikkService historikkService;

    @Inject
    public HistoryController(
            TilgangService tilgangService,
            AktoerService aktoerService,
            MoteService moteService,
            HistorikkService historikkService
    ) {
        this.tilgangService = tilgangService;
        this.aktoerService = aktoerService;
        this.moteService = moteService;
        this.historikkService = historikkService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<RSHistorikk> getHistory(
            @RequestParam(value = "fnr") String fnr
    ) {
        Fnr personFnr = Fnr.of(fnr);

        tilgangService.throwExceptionIfVeilederWithoutAccess(personFnr);

        List<Mote> moter = moteService.findMoterByBrukerAktoerId(aktoerService.hentAktoerIdForIdent(personFnr.getFnr()));
        List<RSHistorikk> historikk = new ArrayList<>();
        historikk.addAll(historikkService.opprettetHistorikk(moter));
        historikk.addAll(historikkService.flereTidspunktHistorikk(moter));
        historikk.addAll(historikkService.avbruttHistorikk(moter));
        historikk.addAll(historikkService.bekreftetHistorikk(moter));
        return historikk;
    }
}

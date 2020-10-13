package no.nav.syfo.api.ressurser.azuread;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.syfo.consumer.pdl.PdlConsumer;
import no.nav.syfo.domain.Fodselsnummer;
import no.nav.syfo.api.domain.RSHistorikk;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.service.*;
import no.nav.syfo.consumer.veiledertilgang.VeilederTilgangConsumer;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static no.nav.syfo.api.auth.OIDCIssuer.AZURE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@ProtectedWithClaims(issuer = AZURE)
@RequestMapping(value = "/api/internad/historikk")
public class HistoryController {

    private VeilederTilgangConsumer tilgangService;

    private MoteService moteService;

    private PdlConsumer pdlConsumer;

    private HistorikkService historikkService;

    @Inject
    public HistoryController(
            VeilederTilgangConsumer tilgangService,
            MoteService moteService,
            PdlConsumer pdlConsumer,
            HistorikkService historikkService
    ) {
        this.tilgangService = tilgangService;
        this.pdlConsumer = pdlConsumer;
        this.moteService = moteService;
        this.historikkService = historikkService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<RSHistorikk> getHistory(
            @RequestParam(value = "fnr") String fnr
    ) {
        Fodselsnummer personFnr = new Fodselsnummer(fnr);

        tilgangService.throwExceptionIfVeilederWithoutAccess(personFnr);

        List<Mote> moter = moteService.findMoterByBrukerAktoerId(pdlConsumer.aktorId(personFnr).getValue());
        List<RSHistorikk> historikk = new ArrayList<>();
        historikk.addAll(historikkService.opprettetHistorikk(moter));
        historikk.addAll(historikkService.flereTidspunktHistorikk(moter));
        historikk.addAll(historikkService.avbruttHistorikk(moter));
        historikk.addAll(historikkService.bekreftetHistorikk(moter));
        return historikk;
    }
}

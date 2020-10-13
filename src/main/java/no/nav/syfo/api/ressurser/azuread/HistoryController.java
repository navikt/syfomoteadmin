package no.nav.syfo.api.ressurser.azuread;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.syfo.consumer.aktorregister.AktorregisterConsumer;
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

    private AktorregisterConsumer aktorregisterConsumer;

    private MoteService moteService;

    private HistorikkService historikkService;

    @Inject
    public HistoryController(
            VeilederTilgangConsumer tilgangService,
            AktorregisterConsumer aktorregisterConsumer,
            MoteService moteService,
            HistorikkService historikkService
    ) {
        this.tilgangService = tilgangService;
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.moteService = moteService;
        this.historikkService = historikkService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<RSHistorikk> getHistory(
            @RequestParam(value = "fnr") String fnr
    ) {
        Fodselsnummer personFnr = new Fodselsnummer(fnr);

        tilgangService.throwExceptionIfVeilederWithoutAccess(personFnr);

        List<Mote> moter = moteService.findMoterByBrukerAktoerId(aktorregisterConsumer.getAktorIdForFodselsnummer(personFnr));
        List<RSHistorikk> historikk = new ArrayList<>();
        historikk.addAll(historikkService.opprettetHistorikk(moter));
        historikk.addAll(historikkService.flereTidspunktHistorikk(moter));
        historikk.addAll(historikkService.avbruttHistorikk(moter));
        historikk.addAll(historikkService.bekreftetHistorikk(moter));
        return historikk;
    }
}

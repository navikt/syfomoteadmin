package no.nav.syfo.api.ressurser;

import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.api.domain.RSHistorikk;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.HistorikkService;
import no.nav.syfo.service.MoteService;
import no.nav.syfo.service.TilgangService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ForbiddenException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static no.nav.syfo.oidc.OIDCIssuer.INTERN;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/historikk")
@ProtectedWithClaims(issuer = INTERN)
public class HistorikkRessurs {

    private TilgangService tilgangService;

    private AktoerService aktoerService;

    private MoteService moteService;

    private HistorikkService historikkService;

    @Inject
    public HistorikkRessurs(
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
    public List<RSHistorikk> hentHistorikk(@RequestParam(value = "fnr") String fnr) {
        if ("true".equals(System.getProperty("LOCAL_MOCK"))) {
            return singletonList(
                    new RSHistorikk()
                            .tekst("Her kommer noe historikk fra møteplanleggeren")
                            .tidspunkt(now().plusDays(2))
            );
        }

        tilgangService.kastExceptionHvisIkkeVeilederHarTilgangTilPerson(fnr);

        List<Mote> moter = moteService.findMoterByBrukerAktoerId(aktoerService.hentAktoerIdForIdent(fnr));
        List<RSHistorikk> historikk = new ArrayList<>();
        historikk.addAll(historikkService.opprettetHistorikk(moter));
        historikk.addAll(historikkService.flereTidspunktHistorikk(moter));
        historikk.addAll(historikkService.avbruttHistorikk(moter));
        historikk.addAll(historikkService.bekreftetHistorikk(moter));
        return historikk;
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

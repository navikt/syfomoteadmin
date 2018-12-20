package no.nav.syfo.api.ressurser;

import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.api.domain.RSHistorikk;
import no.nav.syfo.service.HistorikkService;
import no.nav.syfo.service.TilgangService;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.MoteService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Component
@Path("/historikk")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class HistorikkRessurs {

    @Inject
    private TilgangService tilgangService;
    @Inject
    private AktoerService aktoerService;
    @Inject
    private MoteService moteService;
    @Inject
    private HistorikkService historikkService;

    @GET
    public List<RSHistorikk> hentHistorikk(@QueryParam("fnr") String fnr) {
        if ("true".equals(System.getProperty("LOCAL_MOCK"))) {
            return singletonList(
                    new RSHistorikk()
                            .tekst("Her kommer noe historikk fra møteplanleggeren")
                            .tidspunkt(now().plusDays(2))
            );
        }

        if (tilgangService.sjekkTilgangTilPerson(fnr).getStatus() == 200) {
            List<Mote> moter = moteService.findMoterByBrukerAktoerId(aktoerService.hentAktoerIdForIdent(fnr));
            List<RSHistorikk> historikk = new ArrayList<>();
            historikk.addAll(historikkService.opprettetHistorikk(moter));
            historikk.addAll(historikkService.flereTidspunktHistorikk(moter));
            historikk.addAll(historikkService.avbruttHistorikk(moter));
            historikk.addAll(historikkService.bekreftetHistorikk(moter));
            return historikk;
        } else {
            throw new ForbiddenException("Innlogget bruker har ikke tilgang til denne informasjonen");
        }
    }

}

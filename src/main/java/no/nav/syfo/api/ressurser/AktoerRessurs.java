package no.nav.syfo.api.ressurser;

import no.nav.syfo.api.domain.RSAktor;
import no.nav.syfo.service.TilgangService;
import no.nav.syfo.service.AktoerService;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/aktor/{aktorId}")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class AktoerRessurs {

    @Inject
    private AktoerService aktoerService;

    @Inject
    private TilgangService tilgangService;

    @GET
    public RSAktor get(@PathParam("aktorId") String aktorId) {
        final String fnr = aktoerService.hentFnrForAktoer(aktorId);
        if (tilgangService.sjekkTilgangTilPerson(fnr).getStatus() == 200) {
            return new RSAktor().fnr(fnr);
        } else {
            throw new ForbiddenException("Innlogget bruker har ikke tilgang til denne personen");
        }
    }

}

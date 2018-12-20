package no.nav.syfo.api.ressurser.actions;

import no.nav.metrics.aspects.Count;
import no.nav.metrics.aspects.Timed;
import no.nav.syfo.api.domain.RSOverforMoter;
import no.nav.syfo.service.MoteService;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.syfo.util.SubjectHandlerUtil.getUserId;

@Controller
@Path("/actions/moter")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class MoterActions {

    @Inject
    private MoteService moteService;

    @POST
    @Timed(name = "overfor")
    @Count(name = "overfor")
    @Path("/overfor")
    public void overforMoter(RSOverforMoter rsOverforMoter) {
        rsOverforMoter.moteUuidListe.forEach(moteUuid -> moteService.overforMoteTil(moteUuid, getUserId()));
    }
}

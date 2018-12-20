package no.nav.syfo.api.ressurser.actions;

import no.nav.metrics.aspects.Count;
import no.nav.metrics.aspects.Timed;
import no.nav.syfo.api.domain.nyttmoterequest.RSNyttAlternativ;
import no.nav.syfo.service.ArenaService;
import no.nav.syfo.service.MoteService;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.syfo.api.mappers.RSNyttMoteMapper.opprett2TidOgSted;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.SubjectHandlerUtil.getUserId;

@Controller
@Path("/moter/{moteUuid}")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class MoteActions {

    @Inject
    private MoteService moteService;

    @Inject
    private ArenaService arenaService;

    @POST
    @Timed(name = "avbrytMote")
    @Count(name = "avbrytMote")
    @Path("/avbryt")
    public void avbryt(@PathParam("moteUuid") String moteUuid, @QueryParam("varsle") boolean varsle) {
        moteService.avbrytMote(moteUuid, varsle, getUserId());
    }

    @POST
    @Timed(name = "bekreftMote")
    @Count(name = "bekreftMote")
    @Path("/bekreft")
    public void bekreft(@PathParam("moteUuid") String moteUuid, @QueryParam("valgtAlternativId") Long tidOgStedId) {
        moteService.bekreftMote(moteUuid, tidOgStedId, getUserId());
    }

    @POST
    @Timed(name = "nyeAlternativer")
    @Count(name = "nyeAlternativer")
    @Path("/nyealternativer")
    public void nyeAlternativer(@PathParam("moteUuid") String moteUuid, List<RSNyttAlternativ> alternativer) {
        moteService.nyeAlternativer(moteUuid, mapListe(alternativer, opprett2TidOgSted), getUserId());
    }

    @POST
    @Timed(name = "opprettSanksjonsoppgave")
    @Count(name = "opprettSanksjonsoppgave")
    @Path("/opprettSanksjonsoppgave")
    public void opprettSanksjonsoppgave(@PathParam("moteUuid") String moteUuid) {
        arenaService.bestillOppgave(moteUuid);
    }
}

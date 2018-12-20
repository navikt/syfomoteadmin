package no.nav.syfo.api.ressurser;


import no.nav.syfo.api.domain.RSVeilederInfo;
import no.nav.syfo.service.VeilederService;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.syfo.util.SubjectHandlerUtil.getUserId;

@Controller
@Path("/veilederinfo")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class VeilederRessurs {

    @Inject
    private VeilederService veilederService;

    @GET
    public RSVeilederInfo hentNavn() {
        return hentIdent(getUserId());
    }

    @GET
    @Path("/{ident}")
    public RSVeilederInfo hentIdent(@PathParam("ident") String ident) {
        return new RSVeilederInfo()
                .navn(veilederService.hentVeileder(ident).navn)
                .ident(ident);
    }

}

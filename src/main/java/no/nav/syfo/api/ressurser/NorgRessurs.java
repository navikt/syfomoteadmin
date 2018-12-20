package no.nav.syfo.api.ressurser;

import no.nav.syfo.api.domain.RSEnheter;
import no.nav.syfo.service.NorgService;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.syfo.api.mappers.RSEnhetMapper.enhet2rs;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.SubjectHandlerUtil.getUserId;

@Controller
@Path("/enheter")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class NorgRessurs {

    @Inject
    private NorgService norgService;

    @GET
    public RSEnheter hentEnheter() {
        return new RSEnheter()
                .enhetliste(mapListe(norgService.hentVeiledersNavEnheter(getUserId()), enhet2rs));
    }
}

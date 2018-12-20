package no.nav.syfo.api.ressurser;


import no.nav.syfo.api.domain.RSVirksomhet;
import no.nav.syfo.service.OrganisasjonService;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.text.WordUtils.capitalize;

@Controller
@Path("/virksomhet/{orgnummer}")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class EregRessurs {

    @Inject
    private OrganisasjonService organisasjonService;

    @GET
    public RSVirksomhet hentOrganisasjonsNavn(@PathParam("orgnummer") String orgnummer) {
        return new RSVirksomhet().navn(capitalize(organisasjonService.hentNavn(orgnummer).toLowerCase()));
    }
}

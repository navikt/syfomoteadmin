package no.nav.syfo.api.ressurser;

import no.nav.syfo.api.domain.bruker.BrukerMote;
import no.nav.syfo.api.domain.bruker.BrukerMoteSvar;
import no.nav.syfo.api.domain.bruker.BrukerOppdaterMoteSvar;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.BrukertilgangService;
import no.nav.syfo.service.MoteBrukerService;
import no.nav.syfo.util.Brukerkontekst;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import java.io.IOException;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.syfo.util.SubjectHandlerUtil.getUserId;
import static org.springframework.http.HttpStatus.*;

@Component
@Path("/bruker")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Controller
public class BrukerMoterRessurs {

    @Inject
    private AktoerService aktoerService;
    @Inject
    private BrukertilgangService brukertilgangService;
    @Inject
    private MoteBrukerService moteBrukerService;

    @GET
    @Path("/arbeidsgiver/moter")
    public List<BrukerMote> hentMoter() {
        String innloggetIdent = getUserId();
        String innloggetAktorId = aktoerService.hentAktoerIdForIdent(innloggetIdent);

        brukertilgangService.kastExceptionHvisIkkeTilgang(innloggetIdent);

        return moteBrukerService.hentBrukerMoteListe(innloggetAktorId, Brukerkontekst.ARBEIDSGIVER);
    }

    @GET
    @Path("/arbeidstaker/moter/siste")
    public BrukerMote hentSisteMote() {
        String innloggetIdent = getUserId();
        String innloggetAktorId = aktoerService.hentAktoerIdForIdent(innloggetIdent);

        brukertilgangService.kastExceptionHvisIkkeTilgang(innloggetIdent);

        return moteBrukerService.hentSisteBrukerMote(innloggetAktorId, Brukerkontekst.ARBEIDSTAKER);
    }

    @POST
    @Path("/moter/{moteUuid}/send")
    public BrukerOppdaterMoteSvar oppdaterMotedeltaker(@PathParam("moteUuid") final String moteUuid, BrukerMoteSvar motesvar) throws Exception {
        return moteBrukerService.sendSvar(moteUuid, motesvar);
    }

    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    void handleBadRequests(HttpServletResponse response) throws IOException {
        response.sendError(BAD_REQUEST.value(), "Vi kunne ikke tolke inndataene :/");
    }

    @ExceptionHandler({NotFoundException.class})
    void handleNotFoundRequests(HttpServletResponse response) throws IOException {
        response.sendError(NOT_FOUND.value(), "Fant ikke møte");
    }

    @ExceptionHandler({ForbiddenException.class})
    void handleForbiddenRequests(HttpServletResponse response) throws IOException {
        response.sendError(FORBIDDEN.value(), "Handling er forbudt");
    }
}

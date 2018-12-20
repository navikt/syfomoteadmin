package no.nav.syfo.api.ressurser;

import no.nav.syfo.api.domain.RSBrukerPaaEnhet;
import no.nav.syfo.service.TilgangService;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.BrukerprofilService;
import no.nav.syfo.service.EgenAnsattService;
import no.nav.syfo.service.MotedeltakerService;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/enhet")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class EnhetRessurs {

    @Inject
    private MotedeltakerService motedeltakerService;

    @Inject
    private AktoerService aktoerService;

    @Inject
    private BrukerprofilService brukerprofilService;

    @Inject
    private EgenAnsattService egenAnsattService;

    @Inject
    private TilgangService tilgangService;

    @GET
    @Path("/{enhet}/moter/brukere")
    public List<RSBrukerPaaEnhet> hentSykmeldteMedAktiveMoterForEnhet(@PathParam("enhet") String enhet) {
        if (tilgangService.sjekkTilgangTilEnhet(enhet).getStatus() != 200)
            throw new ForbiddenException("Innlogget bruker har ikke tilgang til denne informasjonen");
        return motedeltakerService.sykmeldteMedMoteHvorBeggeHarSvart(enhet)
                .stream()
                .map(motedeltakerAktorId -> aktoerService.hentFnrForAktoer(motedeltakerAktorId))
                .map(sykmeldtFnr -> new RSBrukerPaaEnhet()
                        .fnr(sykmeldtFnr)
                        .skjermetEllerEgenAnsatt(sykmeldtErDiskresjonsmerketEllerEgenAnsatt(sykmeldtFnr)))
                .collect(toList());
    }

    private boolean sykmeldtErDiskresjonsmerketEllerEgenAnsatt(String fnr) {
        return brukerprofilService.hentBruker(fnr).skjermetBruker() || egenAnsattService.erEgenAnsatt(fnr);
    }

}

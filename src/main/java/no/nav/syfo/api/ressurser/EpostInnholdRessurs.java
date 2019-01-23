package no.nav.syfo.api.ressurser;

import no.nav.syfo.domain.model.*;
import no.nav.syfo.repository.model.PEpost;
import no.nav.syfo.api.domain.RSEpostInnhold;
import no.nav.syfo.service.TilgangService;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.MoteService;
import no.nav.syfo.service.varselinnhold.ArbeidsgiverVarselService;
import no.nav.syfo.util.ServiceVarselInnholdUtil;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.syfo.util.ServiceVarselInnholdUtil.*;

@Controller
@Path("/epostinnhold/{varseltype}")
@Consumes(APPLICATION_JSON)
@Produces("application/json;charset=UTF-8")
public class EpostInnholdRessurs {

    static final String BRUKER = "Bruker";
    static final String BEKREFTET = "BEKREFTET";
    private static final String AVBRUTT = "AVBRUTT";

    @Inject
    private MoteService moteService;
    @Inject
    private TilgangService tilgangService;
    @Inject
    private AktoerService aktoerService;
    @Inject
    private ArbeidsgiverVarselService arbeidsgiverVarselService;

    @GET
    public RSEpostInnhold genererEpostInnholdForFrontend(@PathParam("varseltype") String varseltype,
                                                         @QueryParam("motedeltakeruuid") String motedeltakeruuid,
                                                         @QueryParam("valgtAlternativId") String valgtAlternativId) {
        Mote Mote = moteService.findMoteByMotedeltakerUuid(motedeltakeruuid);
        String sykmeldtFnr = aktoerService.hentFnrForAktoer(Mote.sykmeldt().aktorId);
        if (tilgangService.sjekkTilgangTilPerson(sykmeldtFnr).getStatus() == 200) {
            Motedeltaker motedeltaker = Mote.motedeltakere.stream()
                    .filter(m -> m.uuid.equals(motedeltakeruuid))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Fant ikke møtedeltakeren!"));

            if (valgtAlternativId != null) {
                TidOgSted valgtTidOgSted = motedeltaker.tidOgStedAlternativer.stream()
                        .filter(tidOgSted -> Long.toString(tidOgSted.id).equals(valgtAlternativId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Fant ikke det valgte tidogsted-alternativet!!"));
                Mote.valgtTidOgSted(valgtTidOgSted);
            }

            if (BRUKER.equals(motedeltaker.motedeltakertype)) {
                if (AVBRUTT.equals(varseltype)) {
                    if (Mote.status.equals(MoteStatus.BEKREFTET)) {
                        ServiceVarselInnholdUtil.ServiceVarsel avbrytEpost = avbrytBekreftetEpost();
                        return new RSEpostInnhold().innhold(avbrytEpost.innhold).emne(avbrytEpost.emne);
                    }
                    ServiceVarselInnholdUtil.ServiceVarsel avbrytEpost = avbrytEpost();
                    return new RSEpostInnhold().innhold(avbrytEpost.innhold).emne(avbrytEpost.emne);
                } else if (BEKREFTET.equals(varseltype)) {
                    ServiceVarselInnholdUtil.ServiceVarsel bekreftetEpost = bekreftetEpost(Mote);
                    return new RSEpostInnhold().innhold(bekreftetEpost.innhold).emne(bekreftetEpost.emne);
                }
            }

            Varseltype type = Varseltype.valueOf(String.valueOf(varseltype.toUpperCase()));
            if (type.equals(Varseltype.AVBRUTT) && Mote.status.equals(MoteStatus.BEKREFTET)) {
                type = Varseltype.AVBRUTT_BEKREFTET;
            }
            PEpost epost = arbeidsgiverVarselService.varselinnhold(type, Mote, false);
            return new RSEpostInnhold().emne(epost.emne).innhold(finnInnholdIHTMLTag(epost.innhold));
        } else {
            throw new ForbiddenException("Innlogget bruker har ikke tilgang til denne informasjonen");
        }
    }

    //vil helst ikke dra inn XPath bare for dette. Bør vurderes om det blir mer slikt.
    private String finnInnholdIHTMLTag(String innhold) {
        innhold = innhold.split("<html>")[1];
        innhold = innhold.split("</html>")[0];
        return innhold;
    }
}

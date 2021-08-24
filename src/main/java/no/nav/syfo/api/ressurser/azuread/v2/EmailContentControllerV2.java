package no.nav.syfo.api.ressurser.azuread.v2;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.api.domain.RSEpostInnhold;
import no.nav.syfo.consumer.pdl.PdlConsumer;
import no.nav.syfo.consumer.veiledertilgang.VeilederTilgangConsumer;
import no.nav.syfo.domain.AktorId;
import no.nav.syfo.domain.Fodselsnummer;
import no.nav.syfo.domain.model.*;
import no.nav.syfo.repository.model.PEpost;
import no.nav.syfo.service.MoteService;
import no.nav.syfo.service.varselinnhold.ArbeidsgiverVarselService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.api.auth.OIDCIssuer.VEILEDER_AZURE_V2;
import static no.nav.syfo.api.auth.OIDCUtil.getSubjectInternAzureV2;
import static no.nav.syfo.util.ServiceVarselInnholdUtil.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/internad/v2/epostinnhold/{varseltype}")
@ProtectedWithClaims(issuer = VEILEDER_AZURE_V2)
public class EmailContentControllerV2 {

    public static final String BRUKER = "Bruker";
    public static final String BEKREFTET = "BEKREFTET";
    private static final String AVBRUTT = "AVBRUTT";

    private final OIDCRequestContextHolder contextHolder;

    private final MoteService moteService;

    private final PdlConsumer pdlConsumer;

    private final VeilederTilgangConsumer tilgangService;

    private final ArbeidsgiverVarselService arbeidsgiverVarselService;

    @Inject
    public EmailContentControllerV2(
            OIDCRequestContextHolder contextHolder,
            MoteService moteService,
            PdlConsumer pdlConsumer,
            VeilederTilgangConsumer tilgangService,
            ArbeidsgiverVarselService arbeidsgiverVarselService
    ) {
        this.contextHolder = contextHolder;
        this.moteService = moteService;
        this.pdlConsumer = pdlConsumer;
        this.tilgangService = tilgangService;
        this.arbeidsgiverVarselService = arbeidsgiverVarselService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public RSEpostInnhold getEmailContent(
            @PathVariable(value = "varseltype") String varseltype,
            @RequestParam(value = "motedeltakeruuid") String motedeltakeruuid,
            @RequestParam(value = "valgtAlternativId", required = false) String valgtAlternativId
    ) {
        Mote Mote = moteService.findMoteByMotedeltakerUuid(motedeltakeruuid);
        Fodselsnummer sykmeldtFnr = pdlConsumer.fodselsnummer(new AktorId(Mote.sykmeldt().aktorId));

        tilgangService.throwExceptionIfDeniedAccessAzureOBO(sykmeldtFnr);

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
                    ServiceVarsel avbrytEpost = avbrytBekreftetEpost();
                    return new RSEpostInnhold().innhold(avbrytEpost.innhold).emne(avbrytEpost.emne);
                }
                ServiceVarsel avbrytEpost = avbrytEpost();
                return new RSEpostInnhold().innhold(avbrytEpost.innhold).emne(avbrytEpost.emne);
            } else if (BEKREFTET.equals(varseltype)) {
                ServiceVarsel bekreftetEpost = bekreftetEpost(Mote);
                return new RSEpostInnhold().innhold(bekreftetEpost.innhold).emne(bekreftetEpost.emne);
            }
        }

        Varseltype type = Varseltype.valueOf(varseltype.toUpperCase());
        if (type.equals(Varseltype.AVBRUTT) && Mote.status.equals(MoteStatus.BEKREFTET)) {
            type = Varseltype.AVBRUTT_BEKREFTET;
        }
        PEpost epost = arbeidsgiverVarselService.varselinnhold(type, Mote, getSubjectInternAzureV2(contextHolder));
        return new RSEpostInnhold().emne(epost.emne).innhold(finnInnholdIHTMLTag(epost.innhold));
    }

    //vil helst ikke dra inn XPath bare for dette. Bør vurderes om det blir mer slikt.
    private String finnInnholdIHTMLTag(String innhold) {
        innhold = innhold.split("<html>")[1];
        innhold = innhold.split("</html>")[0];
        return innhold;
    }
}

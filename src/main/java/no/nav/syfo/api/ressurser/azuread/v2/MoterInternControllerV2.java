package no.nav.syfo.api.ressurser.azuread.v2;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.api.domain.RSMote;
import no.nav.syfo.api.domain.RSTilgang;
import no.nav.syfo.consumer.axsys.AxsysConsumer;
import no.nav.syfo.consumer.pdl.PdlConsumer;
import no.nav.syfo.consumer.veiledertilgang.VeilederTilgangConsumer;
import no.nav.syfo.domain.AktorId;
import no.nav.syfo.domain.Fodselsnummer;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.MotedeltakerAktorId;
import no.nav.syfo.metric.Metric;
import no.nav.syfo.service.HendelseService;
import no.nav.syfo.service.MoteService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.status;
import static no.nav.syfo.api.auth.OIDCIssuer.VEILEDER_AZURE_V2;
import static no.nav.syfo.api.auth.OIDCUtil.getSubjectInternAzureV2;
import static no.nav.syfo.api.mappers.RSMoteMapper.mote2rs;
import static no.nav.syfo.util.MapUtil.mapListe;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.StringUtils.isEmpty;

@RestController
@ProtectedWithClaims(issuer = VEILEDER_AZURE_V2)
@RequestMapping(value = "/api/internad/v2/moter")
public class MoterInternControllerV2 {

    private final OIDCRequestContextHolder contextHolder;
    private final Metric metric;
    private final MoteService moteService;
    private final HendelseService hendelseService;
    private final AxsysConsumer axsysConsumer;
    private final PdlConsumer pdlConsumer;
    private final VeilederTilgangConsumer tilgangService;

    @Inject
    public MoterInternControllerV2(
            OIDCRequestContextHolder contextHolder,
            Metric metric,
            MoteService moteService,
            HendelseService hendelseService,
            AxsysConsumer axsysConsumer,
            PdlConsumer pdlConsumer,
            VeilederTilgangConsumer tilgangService
    ) {
        this.contextHolder = contextHolder;
        this.metric = metric;
        this.moteService = moteService;
        this.hendelseService = hendelseService;
        this.axsysConsumer = axsysConsumer;
        this.pdlConsumer = pdlConsumer;
        this.tilgangService = tilgangService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<RSMote> hentMoter(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "fnr", required = false) String fnr,
            @RequestParam(value = "veiledersmoter", required = false) Boolean veiledersMoter,
            @RequestParam(value = "navenhet", required = false) String navenhet,
            @RequestParam(value = "henttpsdata", required = false) boolean hentTpsData
    ) {
        List<Mote> moter = new ArrayList<>();
        List<Mote> moterByFnr = new ArrayList<>();

        if (!isEmpty(fnr)) {
            boolean erBrukerSkjermet = pdlConsumer.isKode6Or7(fnr);
            if (erBrukerSkjermet || !tilgangService.hasVeilederAccessToPersonWithAzureOBO(new Fodselsnummer(fnr))) {
                throw new ForbiddenException(status(FORBIDDEN)
                        .entity(erBrukerSkjermet ?
                                new RSTilgang()
                                        .harTilgang(false)
                                        .begrunnelse("KODE7")
                                : new RSTilgang().harTilgang(false))
                        .type(APPLICATION_JSON)
                        .build());
            } else {
                moterByFnr.addAll(
                        moteService.findMoterByBrukerAktoerId(
                                pdlConsumer.aktorId(new Fodselsnummer(fnr)).getValue()
                        )
                );
                moter.addAll(moterByFnr);
            }
        }

        List<Mote> moterByVeileder = new ArrayList<>();
        if (!isEmpty(veiledersMoter) && veiledersMoter) {
            moterByVeileder.addAll(moteService.maxTwoMonthOldMoterVeileder(getSubjectInternAzureV2(contextHolder)));
            moter.addAll(moterByVeileder);
        }

        List<Mote> moterByNavEnhet = new ArrayList<>();
        if (!isEmpty(navenhet) && hoererNavEnhetTilBruker(navenhet, getSubjectInternAzureV2(contextHolder))) {
            moterByNavEnhet.addAll(moteService.maxTwoMonthOldMoterEnhet(navenhet));
            moter.addAll(moterByNavEnhet);
        }

        if (!isEmpty(fnr)) {
            moter = intersection(moter, moterByFnr);
        }
        if (!isEmpty(veiledersMoter) && veiledersMoter) {
            moter = intersection(moter, moterByVeileder);
        }
        if (!isEmpty(navenhet)) {
            moter = intersection(moter, moterByNavEnhet);
        }

        moter = moter.stream()
                .filter(mote -> !pdlConsumer.isKode6Or7(pdlConsumer.fodselsnummer(new AktorId(mote.sykmeldt().aktorId)).getValue()))
                .filter(mote -> tilgangService.hasVeilederAccessToPersonWithAzureOBO(
                        new Fodselsnummer(pdlConsumer.fodselsnummer(new AktorId(mote.sykmeldt().aktorId)).getValue()))
                )
                .collect(toList());

        if (limit != null) {
            moter = moter.stream()
                    .sorted((o1, o2) -> o2.opprettetTidspunkt.compareTo(o1.opprettetTidspunkt))
                    .limit(limit).collect(toList());
        }
        if (hentTpsData) {
            moter = populerMedNavn(moter);
        }

        metric.tellEndepunktKall("hent_moter");

        return mapListe(moter, mote2rs)
                .stream()
                .map(rsMote -> rsMote
                        .sistEndret(hendelseService.sistEndretMoteStatus(rsMote.id)
                                .orElse(rsMote.opprettetTidspunkt))
                        .trengerBehandling(trengerBehandling(rsMote)))
                .collect(toList());
    }

    private boolean trengerBehandling(RSMote rsMote) {
        return moteService.harAlleSvartPaSisteForesporselRs(rsMote)
                && !"bekreftet".equalsIgnoreCase(rsMote.status)
                && !"avbrutt".equalsIgnoreCase(rsMote.status);
    }

    private List<Mote> populerMedNavn(List<Mote> moter) {
        return moter.stream()
                .map(mote -> mote.motedeltakere(mote.motedeltakere.stream()
                        .map(motedeltaker -> {
                            if (motedeltaker instanceof MotedeltakerAktorId) {
                                MotedeltakerAktorId sykmeldt = (MotedeltakerAktorId) motedeltaker;
                                return sykmeldt.navn(pdlConsumer.fullName(pdlConsumer.fodselsnummer(new AktorId(sykmeldt.aktorId)).getValue()));
                            }
                            return motedeltaker;
                        })
                        .collect(toList())))
                .collect(toList());
    }

    private List<Mote> intersection(List<Mote> liste1, List<Mote> liste2) {
        return liste2.stream()
                .filter(rsMote1 -> liste1.stream()
                        .anyMatch(rsMote2 -> rsMote2.id.equals(rsMote1.id)))
                .collect(toList());
    }

    public boolean hoererNavEnhetTilBruker(String navEnhet, String veilederIdent) {
        return axsysConsumer.enheter(veilederIdent).stream()
                .anyMatch(enhet -> enhet.getEnhetId().equals(navEnhet));
    }
}

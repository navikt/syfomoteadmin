package no.nav.syfo.api.ressurser.azuread;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.api.domain.RSMote;
import no.nav.syfo.api.domain.RSTilgang;
import no.nav.syfo.api.domain.nyttmoterequest.RSNyttMoteRequest;
import no.nav.syfo.behandlendeenhet.BehandlendeEnhetConsumer;
import no.nav.syfo.domain.model.*;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.repository.dao.MotedeltakerDAO;
import no.nav.syfo.repository.dao.TidOgStedDAO;
import no.nav.syfo.repository.model.PMotedeltakerAktorId;
import no.nav.syfo.repository.model.PMotedeltakerArbeidsgiver;
import no.nav.syfo.service.*;
import no.nav.syfo.service.varselinnhold.ArbeidsgiverVarselService;
import no.nav.syfo.service.varselinnhold.SykmeldtVarselService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.status;
import static no.nav.syfo.api.mappers.RSMoteMapper.mote2rs;
import static no.nav.syfo.api.mappers.RSNyttMoteMapper.opprett2Mote;
import static no.nav.syfo.api.mappers.RSNyttMoteMapper.opprett2TidOgSted;
import static no.nav.syfo.domain.model.MotedeltakerStatus.SENDT;
import static no.nav.syfo.domain.model.Varseltype.OPPRETTET;
import static no.nav.syfo.oidc.OIDCIssuer.AZURE;
import static no.nav.syfo.util.MapUtil.map;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.OIDCUtil.getSubjectInternAzure;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.StringUtils.isEmpty;

@RestController
@ProtectedWithClaims(issuer = AZURE)
@RequestMapping(value = "/api/internad/moter")
public class MoterInternController {

    private OIDCRequestContextHolder contextHolder;
    private Metrikk metrikk;
    private AktoerService aktoerService;
    private final BehandlendeEnhetConsumer behandlendeEnhetConsumer;
    private MoteService moteService;
    private TidOgStedDAO tidOgStedDAO;
    private HendelseService hendelseService;
    private MotedeltakerDAO motedeltakerDAO;
    private NorgService norgService;
    private BrukerprofilService brukerprofilService;
    private PdlConsumer pdlConsumer;
    private VeilederService veilederService;
    private ArbeidsgiverVarselService arbeidsgiverVarselService;
    private SykefravaersoppfoelgingService sykefravaersoppfoelgingService;
    private SykmeldtVarselService sykmeldtVarselService;
    private TilgangService tilgangService;

    @Inject
    public MoterInternController(
            OIDCRequestContextHolder contextHolder,
            Metrikk metrikk,
            AktoerService aktoerService,
            BehandlendeEnhetConsumer behandlendeEnhetConsumer,
            MoteService moteService,
            TidOgStedDAO tidOgStedDAO,
            HendelseService hendelseService,
            MotedeltakerDAO motedeltakerDAO,
            NorgService norgService,
            BrukerprofilService brukerprofilService,
            PdlConsumer pdlConsumer,
            VeilederService veilederService,
            ArbeidsgiverVarselService arbeidsgiverVarselService,
            SykefravaersoppfoelgingService sykefravaersoppfoelgingService,
            SykmeldtVarselService sykmeldtVarselService,
            TilgangService tilgangService
    ) {
        this.contextHolder = contextHolder;
        this.metrikk = metrikk;
        this.aktoerService = aktoerService;
        this.behandlendeEnhetConsumer = behandlendeEnhetConsumer;
        this.moteService = moteService;
        this.tidOgStedDAO = tidOgStedDAO;
        this.hendelseService = hendelseService;
        this.motedeltakerDAO = motedeltakerDAO;
        this.norgService = norgService;
        this.brukerprofilService = brukerprofilService;
        this.pdlConsumer = pdlConsumer;
        this.veilederService = veilederService;
        this.arbeidsgiverVarselService = arbeidsgiverVarselService;
        this.sykefravaersoppfoelgingService = sykefravaersoppfoelgingService;
        this.sykmeldtVarselService = sykmeldtVarselService;
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
            if (erBrukerSkjermet || !tilgangService.harVeilederTilgangTilPersonViaAzure(fnr)) {
                throw new ForbiddenException(status(FORBIDDEN)
                        .entity(erBrukerSkjermet ?
                                new RSTilgang()
                                        .harTilgang(false)
                                        .begrunnelse("KODE7")
                                : new RSTilgang().harTilgang(false))
                        .type(APPLICATION_JSON)
                        .build());
            } else {
                moterByFnr.addAll(moteService.findMoterByBrukerAktoerId(aktoerService.hentAktoerIdForIdent(fnr)));
                moter.addAll(moterByFnr);
            }
        }

        List<Mote> moterByVeileder = new ArrayList<>();
        if (!isEmpty(veiledersMoter) && veiledersMoter) {
            moterByVeileder.addAll(moteService.findMoterByBrukerNavAnsatt(getSubjectInternAzure(contextHolder)));
            moter.addAll(moterByVeileder);
        }

        List<Mote> moterByNavEnhet = new ArrayList<>();
        if (!isEmpty(navenhet) && norgService.hoererNavEnhetTilBruker(navenhet, getSubjectInternAzure(contextHolder))) {
            moterByNavEnhet.addAll(moteService.findMoterByBrukerNavEnhet(navenhet));
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
                .filter(mote -> !pdlConsumer.isKode6Or7(aktoerService.hentFnrForAktoer(mote.sykmeldt().aktorId)))
                .filter(mote -> tilgangService.harVeilederTilgangTilPersonViaAzure(aktoerService.hentFnrForAktoer(mote.sykmeldt().aktorId)))
                .collect(toList());

        if (limit != null) {
            moter = moter.stream()
                    .sorted((o1, o2) -> o2.opprettetTidspunkt.compareTo(o1.opprettetTidspunkt))
                    .limit(limit).collect(toList());
        }
        if (hentTpsData) {
            moter = populerMedTpsData(moter);
        }

        metrikk.tellEndepunktKall("hent_moter");

        return mapListe(moter, mote2rs)
                .stream()
                .map(rsMote -> rsMote
                        .sistEndret(hendelseService.sistEndretMoteStatus(rsMote.id)
                                .orElse(rsMote.opprettetTidspunkt))
                        .trengerBehandling(trengerBehandling(rsMote)))
                .collect(toList());
    }

    private boolean trengerBehandling(RSMote rsMote) {
        return moteService.harAlleSvartPaSisteForesporselRs(rsMote, AZURE)
                && !"bekreftet".equalsIgnoreCase(rsMote.status)
                && !"avbrutt".equalsIgnoreCase(rsMote.status);
    }

    private List<Mote> populerMedTpsData(List<Mote> moter) {
        return moter.stream()
                .map(mote -> mote.motedeltakere(mote.motedeltakere.stream()
                        .map(motedeltaker -> {
                            if (motedeltaker instanceof MotedeltakerAktorId) {
                                MotedeltakerAktorId sykmeldt = (MotedeltakerAktorId) motedeltaker;
                                return sykmeldt.navn(brukerprofilService.finnBrukerPersonnavnByAktoerId(sykmeldt.aktorId));
                            }
                            return motedeltaker;
                        })
                        .collect(toList())))
                .collect(toList());
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public void opprett(
            @RequestBody RSNyttMoteRequest nyttMoteRequest) {
        if (pdlConsumer.isKode6Or7(nyttMoteRequest.fnr) || !tilgangService.harVeilederTilgangTilPersonViaAzure(nyttMoteRequest.fnr)) {
            throw new ForbiddenException();
        } else {
            String aktorId = aktoerService.hentAktoerIdForIdent(nyttMoteRequest.fnr);
            NaermesteLeder naermesteLeder = sykefravaersoppfoelgingService.hentNaermesteLederSomBruker(aktorId, nyttMoteRequest.orgnummer);
            nyttMoteRequest.navn(naermesteLeder.navn);
            nyttMoteRequest.epost(naermesteLeder.epost);
            nyttMoteRequest.navEnhet(behandlendeEnhetConsumer.getBehandlendeEnhet(nyttMoteRequest.fnr).getEnhetId());

            Mote nyttMote = map(nyttMoteRequest, opprett2Mote);
            String innloggetIdent = getSubjectInternAzure(contextHolder);
            nyttMote.opprettetAv(innloggetIdent);
            nyttMote.eier(innloggetIdent);
            Mote Mote = moteService.opprettMote(nyttMote);

            List<TidOgSted> alternativer = nyttMoteRequest.alternativer.stream().map(nyttAlternativ -> tidOgStedDAO.create(map(nyttAlternativ, opprett2TidOgSted).moteId(Mote.id))).collect(toList());
            Mote.alternativer(alternativer);

            MotedeltakerAktorId sykmeldt = motedeltakerDAO.create(new PMotedeltakerAktorId()
                    .aktorId(aktorId)
                    .motedeltakertype("Bruker")
                    .moteId(Mote.id)
                    .status(SENDT.name()));
            MotedeltakerArbeidsgiver arbeidsgiver = motedeltakerDAO.create(new PMotedeltakerArbeidsgiver()
                    .navn(naermesteLeder.navn)
                    .orgnummer(naermesteLeder.orgnummer)
                    .epost(naermesteLeder.epost)
                    .motedeltakertype("arbeidsgiver")
                    .moteId(Mote.id)
                    .status(SENDT.name())
            );

            Mote.motedeltakere(asList(
                    sykmeldt,
                    arbeidsgiver
            ));

            arbeidsgiverVarselService.sendVarsel(OPPRETTET, Mote, false, innloggetIdent);
            sykmeldtVarselService.sendVarsel(OPPRETTET, Mote, AZURE);

            metrikk.tellEndepunktKall("opprettet_mote");
        }
    }

    private List<Mote> intersection(List<Mote> liste1, List<Mote> liste2) {
        return liste2.stream()
                .filter(rsMote1 -> liste1.stream()
                        .anyMatch(rsMote2 -> rsMote2.id.equals(rsMote1.id)))
                .collect(toList());
    }
}

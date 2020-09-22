package no.nav.syfo.api.ressurser.azuread;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.aktorregister.domain.*;
import no.nav.syfo.api.domain.*;
import no.nav.syfo.api.domain.nyttmoterequest.RSNyttMoteRequest;
import no.nav.syfo.axsys.AxsysConsumer;
import no.nav.syfo.behandlendeenhet.BehandlendeEnhetConsumer;
import no.nav.syfo.domain.model.*;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.narmesteleder.*;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.repository.dao.*;
import no.nav.syfo.repository.model.*;
import no.nav.syfo.service.*;
import no.nav.syfo.service.varselinnhold.*;
import no.nav.syfo.veiledertilgang.VeilederTilgangConsumer;
import org.springframework.transaction.annotation.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.util.*;

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
    private final AktorregisterConsumer aktorregisterConsumer;
    private final BehandlendeEnhetConsumer behandlendeEnhetConsumer;
    private MoteService moteService;
    private TidOgStedDAO tidOgStedDAO;
    private HendelseService hendelseService;
    private MotedeltakerDAO motedeltakerDAO;
    private AxsysConsumer axsysConsumer;
    private PdlConsumer pdlConsumer;
    private VeilederService veilederService;
    private ArbeidsgiverVarselService arbeidsgiverVarselService;
    private NarmesteLederConsumer narmesteLederConsumer;
    private SykmeldtVarselService sykmeldtVarselService;
    private VeilederTilgangConsumer tilgangService;

    @Inject
    public MoterInternController(
            OIDCRequestContextHolder contextHolder,
            Metrikk metrikk,
            AktorregisterConsumer aktorregisterConsumer,
            BehandlendeEnhetConsumer behandlendeEnhetConsumer,
            MoteService moteService,
            TidOgStedDAO tidOgStedDAO,
            HendelseService hendelseService,
            MotedeltakerDAO motedeltakerDAO,
            AxsysConsumer axsysConsumer,
            PdlConsumer pdlConsumer,
            VeilederService veilederService,
            ArbeidsgiverVarselService arbeidsgiverVarselService,
            NarmesteLederConsumer narmesteLederConsumer,
            SykmeldtVarselService sykmeldtVarselService,
            VeilederTilgangConsumer tilgangService
    ) {
        this.contextHolder = contextHolder;
        this.metrikk = metrikk;
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.behandlendeEnhetConsumer = behandlendeEnhetConsumer;
        this.moteService = moteService;
        this.tidOgStedDAO = tidOgStedDAO;
        this.hendelseService = hendelseService;
        this.motedeltakerDAO = motedeltakerDAO;
        this.axsysConsumer = axsysConsumer;
        this.pdlConsumer = pdlConsumer;
        this.veilederService = veilederService;
        this.arbeidsgiverVarselService = arbeidsgiverVarselService;
        this.narmesteLederConsumer = narmesteLederConsumer;
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
            if (erBrukerSkjermet || !tilgangService.hasVeilederAccessToPerson(fnr)) {
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
                                aktorregisterConsumer.getAktorIdForFodselsnummer(new Fodselsnummer(fnr))
                        )
                );
                moter.addAll(moterByFnr);
            }
        }

        List<Mote> moterByVeileder = new ArrayList<>();
        if (!isEmpty(veiledersMoter) && veiledersMoter) {
            moterByVeileder.addAll(moteService.moterWithMaxTwoMonthOldTidVeileder(getSubjectInternAzure(contextHolder)));
            moter.addAll(moterByVeileder);
        }

        List<Mote> moterByNavEnhet = new ArrayList<>();
        if (!isEmpty(navenhet) && hoererNavEnhetTilBruker(navenhet, getSubjectInternAzure(contextHolder))) {
            moterByNavEnhet.addAll(moteService.moterWithMaxTwoMonthOldTidEnhet(navenhet));
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
                .filter(mote -> !pdlConsumer.isKode6Or7(aktorregisterConsumer.getFnrForAktorId(new AktorId(mote.sykmeldt().aktorId))))
                .filter(mote -> tilgangService.hasVeilederAccessToPerson(
                        aktorregisterConsumer.getFnrForAktorId(new AktorId(mote.sykmeldt().aktorId)))
                )
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
        return moteService.harAlleSvartPaSisteForesporselRs(rsMote)
                && !"bekreftet".equalsIgnoreCase(rsMote.status)
                && !"avbrutt".equalsIgnoreCase(rsMote.status);
    }

    private List<Mote> populerMedTpsData(List<Mote> moter) {
        return moter.stream()
                .map(mote -> mote.motedeltakere(mote.motedeltakere.stream()
                        .map(motedeltaker -> {
                            if (motedeltaker instanceof MotedeltakerAktorId) {
                                MotedeltakerAktorId sykmeldt = (MotedeltakerAktorId) motedeltaker;
                                return sykmeldt.navn(pdlConsumer.fullName(aktorregisterConsumer.getFnrForAktorId(new AktorId(sykmeldt.aktorId))));
                            }
                            return motedeltaker;
                        })
                        .collect(toList())))
                .collect(toList());
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public void opprett(
            @RequestBody RSNyttMoteRequest nyttMoteRequest) {
        if (pdlConsumer.isKode6Or7(nyttMoteRequest.fnr) || !tilgangService.hasVeilederAccessToPerson(nyttMoteRequest.fnr)) {
            throw new ForbiddenException();
        } else {
            String aktorId = aktorregisterConsumer.getAktorIdForFodselsnummer(new Fodselsnummer(nyttMoteRequest.fnr));
            NarmesteLederRelasjon narmesteLederRelasjon = Optional.ofNullable(narmesteLederConsumer.narmesteLederRelasjonLeder(aktorId, nyttMoteRequest.orgnummer))
                    .orElseThrow(() -> new RuntimeException("Fant ikke nærmeste leder"));
            String lederNavn = pdlConsumer.fullName(
                    aktorregisterConsumer.getFnrForAktorId(new AktorId(narmesteLederRelasjon.getNarmesteLederAktorId()))
            );
            nyttMoteRequest.navn(lederNavn);
            nyttMoteRequest.epost(narmesteLederRelasjon.getNarmesteLederEpost());
            nyttMoteRequest.navEnhet(behandlendeEnhetConsumer.getBehandlendeEnhet(nyttMoteRequest.fnr, null).getEnhetId());

            Mote nyttMote = map(nyttMoteRequest, opprett2Mote);
            String innloggetIdent = getSubjectInternAzure(contextHolder);
            nyttMote.opprettetAv(innloggetIdent);
            nyttMote.eier(innloggetIdent);
            Mote mote = opprettNyttMote(
                    nyttMote,
                    nyttMoteRequest,
                    new PMotedeltakerAktorId()
                            .aktorId(aktorId)
                            .motedeltakertype("Bruker")
                            .status(SENDT.name()),
                    new PMotedeltakerArbeidsgiver()
                            .navn(lederNavn)
                            .orgnummer(narmesteLederRelasjon.getOrgnummer())
                            .epost(narmesteLederRelasjon.getNarmesteLederEpost())
                            .motedeltakertype("arbeidsgiver")
                            .status(SENDT.name())
            );
            arbeidsgiverVarselService.sendVarsel(OPPRETTET, mote, false, innloggetIdent);
            sykmeldtVarselService.sendVarsel(OPPRETTET, mote);

            metrikk.tellEndepunktKall("opprettet_mote");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Mote opprettNyttMote(
            Mote nyttMote,
            RSNyttMoteRequest nyttMoteRequest,
            PMotedeltakerAktorId pMotedeltakerAktorId,
            PMotedeltakerArbeidsgiver pMotedeltakerArbeidsgiver
    ) {
        Mote mote = moteService.opprettMote(nyttMote);

        List<TidOgSted> alternativer = nyttMoteRequest.alternativer.stream().map(nyttAlternativ -> tidOgStedDAO.create(map(nyttAlternativ, opprett2TidOgSted).moteId(mote.id))).collect(toList());
        mote.alternativer(alternativer);

        MotedeltakerAktorId sykmeldt = motedeltakerDAO.create(pMotedeltakerAktorId.moteId(mote.id));
        MotedeltakerArbeidsgiver arbeidsgiver = motedeltakerDAO.create(pMotedeltakerArbeidsgiver.moteId(mote.id));

        mote.motedeltakere(asList(
                sykmeldt,
                arbeidsgiver
        ));
        return mote;
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

package no.nav.syfo.service;

import no.nav.syfo.api.domain.RSMote;
import no.nav.syfo.api.domain.RSMotedeltaker;
import no.nav.syfo.consumer.axsys.AxsysConsumer;
import no.nav.syfo.consumer.axsys.AxsysEnhet;
import no.nav.syfo.consumer.dkif.DkifConsumer;
import no.nav.syfo.domain.model.*;
import no.nav.syfo.metric.Metric;
import no.nav.syfo.oversikthendelse.OversikthendelseService;
import no.nav.syfo.repository.dao.*;
import no.nav.syfo.repository.model.PFeedHendelse;
import no.nav.syfo.service.mq.MqStoppRevarslingService;
import no.nav.syfo.service.varselinnhold.ArbeidsgiverVarselService;
import no.nav.syfo.service.varselinnhold.SykmeldtVarselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.ForbiddenException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;
import static no.nav.syfo.domain.model.MoteStatus.*;
import static no.nav.syfo.oversikthendelse.OversikthendelseType.MOTEPLANLEGGER_ALLE_SVAR_BEHANDLET;
import static no.nav.syfo.oversikthendelse.OversikthendelseType.MOTEPLANLEGGER_ALLE_SVAR_MOTTATT;
import static no.nav.syfo.repository.model.PFeedHendelse.FeedHendelseType.ALLE_SVAR_MOTTATT;
import static no.nav.syfo.service.MotedeltakerService.finnAktoerIMote;
import static no.nav.syfo.util.MoteUtilKt.moterAfterGivenDate;
import static no.nav.syfo.util.MoterUtil.filtrerBortAlternativerSomAlleredeErLagret;
import static no.nav.syfo.util.MoterUtil.hentSisteSvartidspunkt;

@Service
public class MoteService {

    private MoteDAO moteDAO;
    private FeedDAO feedDAO;
    private TidOgStedDAO tidOgStedDAO;
    private AxsysConsumer axsysConsumer;
    private HendelseService hendelseService;
    private OversikthendelseService oversikthendelseService;
    private VeilederService veilederService;
    private Metric metric;
    private ArbeidsgiverVarselService arbeidsgiverVarselService;
    private DkifConsumer dkifConsumer;
    private SykmeldtVarselService sykmeldtVarselService;
    private MqStoppRevarslingService mqStoppRevarslingService;
    private FeedService feedService;

    @Autowired
    public MoteService(
            MoteDAO moteDAO,
            FeedDAO feedDAO,
            TidOgStedDAO tidOgStedDAO,
            AxsysConsumer axsysConsumer,
            HendelseService hendelseService,
            OversikthendelseService oversikthendelseService,
            VeilederService veilederService,
            Metric metric,
            ArbeidsgiverVarselService arbeidsgiverVarselService,
            DkifConsumer dkifConsumer,
            SykmeldtVarselService sykmeldtVarselService,
            MqStoppRevarslingService mqStoppRevarslingService,
            FeedService feedService
    ) {
        this.moteDAO = moteDAO;
        this.feedDAO = feedDAO;
        this.tidOgStedDAO = tidOgStedDAO;
        this.hendelseService = hendelseService;
        this.oversikthendelseService = oversikthendelseService;
        this.veilederService = veilederService;
        this.metric = metric;
        this.arbeidsgiverVarselService = arbeidsgiverVarselService;
        this.sykmeldtVarselService = sykmeldtVarselService;
        this.axsysConsumer = axsysConsumer;
        this.mqStoppRevarslingService = mqStoppRevarslingService;
        this.dkifConsumer = dkifConsumer;
        this.feedService = feedService;
    }

    public List<Mote> findMoterByBrukerAktoerId(String aktorId) {
        return moteDAO.findMoterByBrukerAktoerId(aktorId);
    }

    public List<Mote> findMoterByBrukerAktoerIdOgAGOrgnummer(String aktorId, String orgnummer) {
        return moteDAO.findMoterByBrukerAktoerIdOgAGOrgnummer(aktorId, orgnummer);
    }

    public Mote findMoteByMotedeltakerUuid(String brukerUuid) {
        return moteDAO.findMoteByMotedeltakerUuid(brukerUuid);
    }

    public Mote opprettMote(Mote Mote) {
        return moteDAO.create(Mote);
    }

    @Transactional
    public void avbrytMote(String moteUuid, boolean varsle, String veilederIdent) {
        Mote mote = moteDAO.findMoteByUUID(moteUuid);
        mqStoppRevarslingService.stoppReVarsel(finnAktoerIMote(mote).uuid);
        if (varsle) {
            Varseltype varseltype = mote.status.equals(MoteStatus.BEKREFTET) ? Varseltype.AVBRUTT_BEKREFTET : Varseltype.AVBRUTT;
            arbeidsgiverVarselService.sendVarsel(varseltype, mote, false, veilederIdent);
            sykmeldtVarselService.sendVarsel(varseltype, mote);
        }
        moteDAO.setStatus(mote.id, AVBRUTT.name());
        hendelseService.moteStatusEndret(mote.status(AVBRUTT), veilederIdent);
        if (feedService.skalOppretteFeedHendelse(mote, PFeedHendelse.FeedHendelseType.AVBRUTT)) {
            opprettFeedHendelseAvTypen(PFeedHendelse.FeedHendelseType.AVBRUTT, mote, veilederIdent);
        }
        behandleMote(mote);
        metric.reportAntallDagerSiden(hentSisteSvartidspunkt(mote).orElse(mote.opprettetTidspunkt), "antallDagerVeilederSvar");
    }

    @Transactional
    public void bekreftMote(String moteUuid, Long tidOgStedId, String veilederIdent) {
        Mote mote = moteDAO.findMoteByUUID(moteUuid);

        metric.reportAntallDagerSiden(mote.opprettetTidspunkt, "antallDagerForSvar");
        metric.reportAntallDagerSiden(hentSisteSvartidspunkt(mote).orElse(mote.opprettetTidspunkt), "antallDagerVeilederSvar");

        moteDAO.bekreftMote(mote.id, tidOgStedId);
        mote.valgtTidOgSted(mote.alternativer.stream().filter(tidOgSted -> tidOgSted.id.equals(tidOgStedId)).findFirst().orElseThrow(() -> new RuntimeException("Fant ikke tidspunktet!")));

        hendelseService.moteStatusEndret(mote.status(BEKREFTET), veilederIdent);

        mqStoppRevarslingService.stoppReVarsel(finnAktoerIMote(mote).uuid);
        arbeidsgiverVarselService.sendVarsel(Varseltype.BEKREFTET, mote, false, veilederIdent);
        sykmeldtVarselService.sendVarsel(Varseltype.BEKREFTET, mote);
        if (feedService.skalOppretteFeedHendelse(mote, PFeedHendelse.FeedHendelseType.BEKREFTET)) {
            opprettFeedHendelseAvTypen(PFeedHendelse.FeedHendelseType.BEKREFTET, mote, veilederIdent);
        }
        behandleMote(mote);
    }

    @Transactional
    public void nyeAlternativer(String moteUuid, List<TidOgSted> nyeAlternativer, String veilederIdent) {
        Mote mote = moteDAO.findMoteByUUID(moteUuid);

        List<TidOgSted> filtrerBortAlternativerSomAlleredeErLagret = filtrerBortAlternativerSomAlleredeErLagret(nyeAlternativer, mote);
        if (filtrerBortAlternativerSomAlleredeErLagret.isEmpty()) {
            return;
        }

        hendelseService.moteStatusEndret(mote.status(FLERE_TIDSPUNKT), veilederIdent);
        nyeAlternativer.forEach(tidOgSted -> tidOgStedDAO.create(tidOgSted.moteId(mote.id)));

        arbeidsgiverVarselService.sendVarsel(Varseltype.NYE_TIDSPUNKT, mote, false, veilederIdent);
        sykmeldtVarselService.sendVarsel(Varseltype.NYE_TIDSPUNKT, mote);
        if (feedService.skalOppretteFeedHendelse(mote, PFeedHendelse.FeedHendelseType.FLERE_TIDSPUNKT)) {
            opprettFeedHendelseAvTypen(PFeedHendelse.FeedHendelseType.FLERE_TIDSPUNKT, mote, veilederIdent);
        }
        behandleMote(mote);
        metric.reportAntallDagerSiden(hentSisteSvartidspunkt(mote).orElse(mote.opprettetTidspunkt), "antallDagerVeilederSvar");
    }

    private void behandleMote(Mote mote) {
        if (harAlleSvartPaaSisteForespoersel(mote)) {
            oversikthendelseService.sendOversikthendelse(mote, MOTEPLANLEGGER_ALLE_SVAR_BEHANDLET);
        }
    }

    public void overforMoteTil(String moteUuid, String mottakerUserId) {
        Mote Mote = moteDAO.findMoteByUUID(moteUuid);
        List<AxsysEnhet> mottakerEnheter = axsysConsumer.enheter(mottakerUserId);

        boolean mottakerHarIkkeTilgangTilOppgittNavEnhet = mottakerEnheter.stream().noneMatch(enhet -> Mote.navEnhet.equals(enhet.getEnhetId()));
        if (mottakerHarIkkeTilgangTilOppgittNavEnhet) {
            throw new ForbiddenException();
        }

        moteDAO.oppdaterMoteEier(moteUuid, mottakerUserId);
    }

    private boolean harDeltakerSvartTidligereEnnNyesteOpprettet(Motedeltaker motedeltaker, LocalDateTime nyesteAlternativOpprettetTidspunkt) {
        return Optional.ofNullable(motedeltaker.svartTidspunkt)
                .map(svartTidspunkt -> svartTidspunkt.isBefore(nyesteAlternativOpprettetTidspunkt))
                .orElse(true);
    }


    public boolean harAlleSvartPaaSisteForespoersel(Mote Mote) {
        boolean skalSykmeldtHaVarsler = dkifConsumer.kontaktinformasjon(Mote.sykmeldt().aktorId).getKanVarsles();
        LocalDateTime nyesteAlternativOpprettetTidspunkt = Mote.alternativer.stream().sorted((o1, o2) -> o2.created.compareTo(o1.created)).findFirst().get().created;

        return Mote.motedeltakere.stream()
                .filter(erIkkeReservertSykmeldt(skalSykmeldtHaVarsler))
                .noneMatch(deltaker -> harDeltakerSvartTidligereEnnNyesteOpprettet(deltaker, nyesteAlternativOpprettetTidspunkt) ||
                        sisteSvarErIkkeReservertBruk(Mote, skalSykmeldtHaVarsler));
    }

    private boolean sisteSvarErIkkeReservertBruk(Mote Mote, boolean skalSykmeldtHaVarsler) {
        return !skalSykmeldtHaVarsler &&
                Mote.motedeltakere.stream()
                        .filter(motedeltaker -> motedeltaker.svartTidspunkt != null)
                        .sorted((o1, o2) -> o2.svartTidspunkt.compareTo(o1.svartTidspunkt))
                        .findFirst().get().motedeltakertype.equals("Bruker");
    }

    private Predicate<Motedeltaker> erIkkeReservertSykmeldt(boolean skalSykmeldtHaVarsler) {
        return motedeltaker -> skalSykmeldtHaVarsler || !motedeltaker.motedeltakertype().equals("Bruker");
    }

    public boolean harAlleSvartPaSisteForesporselRs(RSMote rsMote) {
        boolean skalSykmeldtHaVarsler = dkifConsumer.kontaktinformasjon(rsMote.aktorId).getKanVarsles();
        LocalDateTime nyesteAlternativOpprettetTidspunkt = rsMote.alternativer.stream().sorted((o1, o2) -> o2.created.compareTo(o1.created)).findFirst().get().created;

        return rsMote.deltakere.stream()
                .filter(erIkkeReservertSykmeldtRs(skalSykmeldtHaVarsler))
                .noneMatch(deltaker -> harDeltakerSvartTidligereEnnNyesteOpprettetRs(deltaker, nyesteAlternativOpprettetTidspunkt));
    }

    private Predicate<RSMotedeltaker> erIkkeReservertSykmeldtRs(boolean skalSykmeldtHaVarsler) {
        return rsMotedeltaker -> skalSykmeldtHaVarsler || !rsMotedeltaker.type().equals("Bruker");
    }

    private boolean harDeltakerSvartTidligereEnnNyesteOpprettetRs(RSMotedeltaker motedeltaker, LocalDateTime nyesteAlternativOpprettetTidspunkt) {
        return Optional.ofNullable(motedeltaker.svartidspunkt)
                .map(svartTidspunkt -> svartTidspunkt.isBefore(nyesteAlternativOpprettetTidspunkt))
                .orElse(true);
    }

    public void svarMottatt(String motedeltakerSomSvarteUuid, Mote mote) {
        Motedeltaker brukerSomSvarte = mote.motedeltakere.stream()
                .filter(deltaker -> deltaker.uuid.equals(motedeltakerSomSvarteUuid))
                .findFirst().get();
        mote.motedeltakere = mote.motedeltakere.stream()
                .map(motedeltaker -> motedeltaker.uuid.equals(brukerSomSvarte.uuid) ? motedeltaker.svartTidspunkt(now()) : motedeltaker)
                .collect(toList());

        if ("Bruker".equals(brukerSomSvarte.motedeltakertype)) {
            mqStoppRevarslingService.stoppReVarsel(brukerSomSvarte.uuid);
            metric.reportAntallDagerSiden(mote.opprettetTidspunkt, "antallDagerBrukerSvar");
        } else if ("arbeidsgiver".equals(brukerSomSvarte.motedeltakertype)) {
            metric.reportAntallDagerSiden(mote.opprettetTidspunkt, "antallDagerArbeidsgiverSvar");
        }

        if (harAlleSvartPaaSisteForespoersel(mote)) {
            oversikthendelseService.sendOversikthendelse(mote, MOTEPLANLEGGER_ALLE_SVAR_MOTTATT);

            feedDAO.createFeedHendelse(new PFeedHendelse()
                    .sistEndretAv(mote.eier)
                    .type(ALLE_SVAR_MOTTATT.name())
                    .uuid(UUID.randomUUID().toString())
                    .moteId(mote.id)
            );
        }
    }

    public Mote findMoteByUUID(String moteUuid) {
        return moteDAO.findMoteByUUID(moteUuid);
    }

    public List<Mote> findMoterByBrukerNavAnsatt(String navansatt) {
        return moteDAO.findMoterByNavAnsatt(navansatt);
    }

    public List<Mote> maxTwoMonthOldMoterVeileder(String navansatt) {
        List<Mote> allNavAnsattMoter = findMoterByBrukerNavAnsatt(navansatt);

        LocalDateTime twoMonthsAgo = LocalDateTime.now().minusMonths(2);
        return moterAfterGivenDate(allNavAnsattMoter, twoMonthsAgo);
    }

    public List<Mote> findMoterByBrukerNavEnhet(String navenhet) {
        return moteDAO.findMoterByNavEnhet(navenhet);
    }

    public List<Mote> maxTwoMonthOldMoterEnhet(String navenhet) {
        List<Mote> allEnhetMoter = findMoterByBrukerNavEnhet(navenhet);

        LocalDateTime twoMonthsAgo = LocalDateTime.now().minusMonths(2);
        return moterAfterGivenDate(allEnhetMoter, twoMonthsAgo);
    }

    private void opprettFeedHendelseAvTypen(PFeedHendelse.FeedHendelseType type, Mote Mote, String veilederIdent) {
        feedDAO.createFeedHendelse(new PFeedHendelse()
                .sistEndretAv(veilederIdent)
                .uuid(feedService.finnNyesteFeedUuidiMote(Mote))
                .type(type.name())
                .moteId(Mote.id)
        );
    }
}

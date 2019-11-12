package no.nav.syfo.service;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.domain.model.*;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.oidc.OIDCIssuer;
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
import static no.nav.syfo.kafka.producer.OversikthendelseType.MOTEPLANLEGGER_ALLE_SVAR_BEHANDLET;
import static no.nav.syfo.kafka.producer.OversikthendelseType.MOTEPLANLEGGER_ALLE_SVAR_MOTTATT;
import static no.nav.syfo.repository.model.PFeedHendelse.FeedHendelseType.ALLE_SVAR_MOTTATT;
import static no.nav.syfo.service.MotedeltakerService.finnAktoerIMote;
import static no.nav.syfo.util.MoterUtil.filtrerBortAlternativerSomAlleredeErLagret;
import static no.nav.syfo.util.MoterUtil.hentSisteSvartidspunkt;

@Service
public class MoteService {

    private MoteDAO moteDAO;
    private FeedDAO feedDAO;
    private TidOgStedDAO tidOgStedDAO;
    private HendelseService hendelseService;
    private OversikthendelseService oversikthendelseService;
    private VeilederService veilederService;
    private Metrikk metrikk;
    private ArbeidsgiverVarselService arbeidsgiverVarselService;
    private SykmeldtVarselService sykmeldtVarselService;
    private NorgService norgService;
    private MqStoppRevarslingService mqStoppRevarslingService;
    private DkifService dkifService;
    private FeedService feedService;

    @Autowired
    public MoteService(
            MoteDAO moteDAO,
            FeedDAO feedDAO,
            TidOgStedDAO tidOgStedDAO,
            HendelseService hendelseService,
            OversikthendelseService oversikthendelseService,
            VeilederService veilederService,
            Metrikk metrikk,
            ArbeidsgiverVarselService arbeidsgiverVarselService,
            SykmeldtVarselService sykmeldtVarselService,
            NorgService norgService,
            MqStoppRevarslingService mqStoppRevarslingService,
            DkifService dkifService,
            FeedService feedService
    ) {
        this.moteDAO = moteDAO;
        this.feedDAO = feedDAO;
        this.tidOgStedDAO = tidOgStedDAO;
        this.hendelseService = hendelseService;
        this.oversikthendelseService = oversikthendelseService;
        this.veilederService = veilederService;
        this.metrikk = metrikk;
        this.arbeidsgiverVarselService = arbeidsgiverVarselService;
        this.sykmeldtVarselService = sykmeldtVarselService;
        this.norgService = norgService;
        this.mqStoppRevarslingService = mqStoppRevarslingService;
        this.dkifService = dkifService;
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
        metrikk.reportAntallDagerSiden(hentSisteSvartidspunkt(mote).orElse(mote.opprettetTidspunkt), "antallDagerVeilederSvar");
    }

    @Transactional
    public void bekreftMote(String moteUuid, Long tidOgStedId, String veilederIdent) {
        Mote mote = moteDAO.findMoteByUUID(moteUuid);

        metrikk.reportAntallDagerSiden(mote.opprettetTidspunkt, "antallDagerForSvar");
        metrikk.reportAntallDagerSiden(hentSisteSvartidspunkt(mote).orElse(mote.opprettetTidspunkt), "antallDagerVeilederSvar");

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
        metrikk.reportAntallDagerSiden(hentSisteSvartidspunkt(mote).orElse(mote.opprettetTidspunkt), "antallDagerVeilederSvar");
    }

    private void behandleMote(Mote mote) {
        if (harAlleSvartPaaSisteForespoersel(mote, OIDCIssuer.INTERN)) {
            oversikthendelseService.sendOversikthendelse(mote, MOTEPLANLEGGER_ALLE_SVAR_BEHANDLET);
        }
    }

    public void overforMoteTil(String moteUuid, String mottakerUserId) {
        Mote Mote = moteDAO.findMoteByUUID(moteUuid);
        List<Enhet> mottakerEnheter = norgService.hentVeiledersNavEnheter(mottakerUserId);

        boolean mottakerHarIkkeTilgangTilOppgittNavEnhet = mottakerEnheter.stream().noneMatch(enhet -> Mote.navEnhet.equals(enhet.enhetId));
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


    public boolean harAlleSvartPaaSisteForespoersel(Mote Mote, String oidcIssuer) {
        boolean skalSykmeldtHaVarsler = dkifService.hentKontaktinfoAktoerId(Mote.sykmeldt().aktorId, oidcIssuer).skalHaVarsel;
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

    public void svarMottatt(String motedeltakerSomSvarteUuid, Mote mote) {
        Motedeltaker brukerSomSvarte = mote.motedeltakere.stream()
                .filter(deltaker -> deltaker.uuid.equals(motedeltakerSomSvarteUuid))
                .findFirst().get();
        mote.motedeltakere = mote.motedeltakere.stream()
                .map(motedeltaker -> motedeltaker.uuid.equals(brukerSomSvarte.uuid) ? motedeltaker.svartTidspunkt(now()) : motedeltaker)
                .collect(toList());

        if ("Bruker".equals(brukerSomSvarte.motedeltakertype)) {
            mqStoppRevarslingService.stoppReVarsel(brukerSomSvarte.uuid);
            metrikk.reportAntallDagerSiden(mote.opprettetTidspunkt, "antallDagerBrukerSvar");
        } else if ("arbeidsgiver".equals(brukerSomSvarte.motedeltakertype)) {
            metrikk.reportAntallDagerSiden(mote.opprettetTidspunkt, "antallDagerArbeidsgiverSvar");
        }

        if (harAlleSvartPaaSisteForespoersel(mote, OIDCIssuer.EKSTERN)) {
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

    public List<Mote> findMoterByBrukerNavEnhet(String navenhet) {
        return moteDAO.findMoterByNavEnhet(navenhet);
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

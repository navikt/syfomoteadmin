package no.nav.syfo.service;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.domain.model.*;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.oidc.OIDCIssuer;
import no.nav.syfo.repository.dao.*;
import no.nav.syfo.repository.model.PFeedHendelse;
import no.nav.syfo.service.mq.MqStoppRevarslingService;
import no.nav.syfo.service.varselinnhold.*;
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
import static no.nav.syfo.repository.model.PFeedHendelse.FeedHendelseType.ALLE_SVAR_MOTTATT;
import static no.nav.syfo.service.MotedeltakerService.finnAktoerIMote;
import static no.nav.syfo.util.MoterUtil.filtrerBortAlternativerSomAlleredeErLagret;
import static no.nav.syfo.util.MoterUtil.hentSisteSvartidspunkt;
import static no.nav.syfo.util.OIDCUtil.getSubjectIntern;

@Service
public class MoteService {

    private OIDCRequestContextHolder contextHolder;
    private MoteDAO moteDAO;
    private FeedDAO feedDAO;
    private TidOgStedDAO tidOgStedDAO;
    private HendelseService hendelseService;
    private VeilederService veilederService;
    private Metrikk metrikk;
    private ArbeidsgiverVarselService arbeidsgiverVarselService;
    private VeilederVarselService veilederVarselService;
    private SykmeldtVarselService sykmeldtVarselService;
    private NorgService norgService;
    private MqStoppRevarslingService mqStoppRevarslingService;
    private DkifService dkifService;
    private FeedService feedService;

    @Autowired
    public MoteService(
            OIDCRequestContextHolder contextHolder,
            MoteDAO moteDAO,
            FeedDAO feedDAO,
            TidOgStedDAO tidOgStedDAO,
            HendelseService hendelseService,
            VeilederService veilederService,
            Metrikk metrikk,
            ArbeidsgiverVarselService arbeidsgiverVarselService,
            VeilederVarselService veilederVarselService,
            SykmeldtVarselService sykmeldtVarselService,
            NorgService norgService,
            MqStoppRevarslingService mqStoppRevarslingService,
            DkifService dkifService,
            FeedService feedService
    ) {
        this.contextHolder = contextHolder;
        this.moteDAO = moteDAO;
        this.feedDAO = feedDAO;
        this.tidOgStedDAO = tidOgStedDAO;
        this.hendelseService = hendelseService;
        this.veilederService = veilederService;
        this.metrikk = metrikk;
        this.arbeidsgiverVarselService = arbeidsgiverVarselService;
        this.veilederVarselService = veilederVarselService;
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
    public void avbrytMote(String moteUuid, boolean varsle, String userId) {
        Mote Mote = moteDAO.findMoteByUUID(moteUuid);
        mqStoppRevarslingService.stoppReVarsel(finnAktoerIMote(Mote).uuid);
        if (varsle) {
            Veileder veileder = veilederService.hentVeileder(userId).mote(Mote);
            Varseltype varseltype = Mote.status.equals(MoteStatus.BEKREFTET) ? Varseltype.AVBRUTT_BEKREFTET : Varseltype.AVBRUTT;
            veilederVarselService.sendVarsel(varseltype, veileder);
            arbeidsgiverVarselService.sendVarsel(varseltype, Mote, false);
            sykmeldtVarselService.sendVarsel(varseltype, Mote);
        }
        moteDAO.setStatus(Mote.id, AVBRUTT.name());
        hendelseService.moteStatusEndret(Mote.status(AVBRUTT));
        if (feedService.skalOppretteFeedHendelse(Mote, PFeedHendelse.FeedHendelseType.AVBRUTT)) {
            opprettFeedHendelseAvTypen(PFeedHendelse.FeedHendelseType.AVBRUTT, Mote);
        }
        metrikk.reportAntallDagerSiden(hentSisteSvartidspunkt(Mote).orElse(Mote.opprettetTidspunkt), "antallDagerVeilederSvar");
    }

    @Transactional
    public void bekreftMote(String moteUuid, Long tidOgStedId, String userId) {
        Mote Mote = moteDAO.findMoteByUUID(moteUuid);

        metrikk.reportAntallDagerSiden(Mote.opprettetTidspunkt, "antallDagerForSvar");
        metrikk.reportAntallDagerSiden(hentSisteSvartidspunkt(Mote).orElse(Mote.opprettetTidspunkt), "antallDagerVeilederSvar");

        moteDAO.bekreftMote(Mote.id, tidOgStedId);
        Mote.valgtTidOgSted(Mote.alternativer.stream().filter(tidOgSted -> tidOgSted.id.equals(tidOgStedId)).findFirst().orElseThrow(() -> new RuntimeException("Fant ikke tidspunktet!")));

        hendelseService.moteStatusEndret(Mote.status(BEKREFTET));
        Veileder veileder = veilederService.hentVeileder(userId)
                .mote(Mote);

        mqStoppRevarslingService.stoppReVarsel(finnAktoerIMote(Mote).uuid);
        veilederVarselService.sendVarsel(Varseltype.BEKREFTET, veileder);
        arbeidsgiverVarselService.sendVarsel(Varseltype.BEKREFTET, Mote, false);
        sykmeldtVarselService.sendVarsel(Varseltype.BEKREFTET, Mote);
        if (feedService.skalOppretteFeedHendelse(Mote, PFeedHendelse.FeedHendelseType.BEKREFTET)) {
            opprettFeedHendelseAvTypen(PFeedHendelse.FeedHendelseType.BEKREFTET, Mote);
        }
    }

    @Transactional
    public void nyeAlternativer(String moteUuid, List<TidOgSted> nyeAlternativer, String userId) {
        Mote Mote = moteDAO.findMoteByUUID(moteUuid);

        List<TidOgSted> filtrerBortAlternativerSomAlleredeErLagret = filtrerBortAlternativerSomAlleredeErLagret(nyeAlternativer, Mote);
        if (filtrerBortAlternativerSomAlleredeErLagret.isEmpty()) {
            return;
        }

        hendelseService.moteStatusEndret(Mote.status(FLERE_TIDSPUNKT));
        nyeAlternativer.forEach(tidOgSted -> tidOgStedDAO.create(tidOgSted.moteId(Mote.id)));
        Veileder veileder = veilederService.hentVeileder(userId).mote(Mote);

        veilederVarselService.sendVarsel(Varseltype.NYE_TIDSPUNKT, veileder);
        arbeidsgiverVarselService.sendVarsel(Varseltype.NYE_TIDSPUNKT, Mote, false);
        sykmeldtVarselService.sendVarsel(Varseltype.NYE_TIDSPUNKT, Mote);
        if (feedService.skalOppretteFeedHendelse(Mote, PFeedHendelse.FeedHendelseType.FLERE_TIDSPUNKT)) {
            opprettFeedHendelseAvTypen(PFeedHendelse.FeedHendelseType.FLERE_TIDSPUNKT, Mote);
        }
        metrikk.reportAntallDagerSiden(hentSisteSvartidspunkt(Mote).orElse(Mote.opprettetTidspunkt), "antallDagerVeilederSvar");
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

    private void opprettFeedHendelseAvTypen(PFeedHendelse.FeedHendelseType type, Mote Mote) {
        feedDAO.createFeedHendelse(new PFeedHendelse()
                .sistEndretAv(getSubjectIntern(contextHolder))
                .uuid(feedService.finnNyesteFeedUuidiMote(Mote))
                .type(type.name())
                .moteId(Mote.id)
        );
    }
}

package no.nav.syfo.service;

import no.nav.syfo.domain.model.*;
import no.nav.syfo.repository.dao.FeedDAO;
import no.nav.syfo.repository.dao.MoteDAO;
import no.nav.syfo.repository.dao.TidOgStedDAO;
import no.nav.syfo.repository.model.PFeedHendelse;
import no.nav.syfo.service.mq.MqStoppRevarslingService;
import no.nav.syfo.service.varselinnhold.ArbeidsgiverVarselService;
import no.nav.syfo.service.varselinnhold.SykmeldtVarselService;
import no.nav.syfo.service.varselinnhold.VeilederVarselService;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;
import static no.nav.syfo.domain.model.MoteStatus.*;
import static no.nav.syfo.repository.model.PFeedHendelse.FeedHendelseType.ALLE_SVAR_MOTTATT;
import static no.nav.syfo.service.MotedeltakerService.finnAktoerIMote;
import static no.nav.syfo.util.MoterUtil.filtrerBortAlternativerSomAlleredeErLagret;
import static no.nav.syfo.util.MoterUtil.hentSisteSvartidspunkt;
import static no.nav.syfo.util.SubjectHandlerUtil.getUserId;

public class MoteService {

    @Inject
    private MoteDAO moteDAO;
    @Inject
    private FeedDAO feedDAO;
    @Inject
    private TidOgStedDAO tidOgStedDAO;
    @Inject
    private HendelseService hendelseService;
    @Inject
    private VeilederService veilederService;
    @Inject
    private MetricsService metricsService;
    @Inject
    private ArbeidsgiverVarselService arbeidsgiverVarselService;
    @Inject
    private VeilederVarselService veilederVarselService;
    @Inject
    private SykmeldtVarselService sykmeldtVarselService;
    @Inject
    private NorgService norgService;
    @Inject
    private MqStoppRevarslingService mqStoppRevarslingService;
    @Inject
    private DkifService dkifService;
    @Inject
    private FeedService feedService;

    public List<Mote> findMoterByBrukerAktoerId(String aktorId) {
        return moteDAO.findMoterByBrukerAktoerId(aktorId);
    }

    public List<Mote> findMoterByBrukerAktoerIdOgAGOrgnummer(String aktorId, String orgnummer) {
        return moteDAO.findMoterByBrukerAktoerIdOgAGOrgnummer(aktorId, orgnummer);
    }

    public Mote findMoteByMotedeltakerUuid(String brukerUuid) {
        return moteDAO.findMoteByMotedeltakerUuid(brukerUuid);
    }

    public Mote opprettMote(Mote mote) {
        return moteDAO.create(mote);
    }

    @Transactional
    public void avbrytMote(String moteUuid, boolean varsle, String userId) {
        Mote mote = moteDAO.findMoteByUUID(moteUuid);
        mqStoppRevarslingService.stoppReVarsel(finnAktoerIMote(mote).uuid);
        if (varsle) {
            Veileder veileder = veilederService.hentVeileder(userId).mote(mote);
            Varseltype varseltype = mote.status.equals(MoteStatus.BEKREFTET) ? Varseltype.AVBRUTT_BEKREFTET : Varseltype.AVBRUTT;
            veilederVarselService.sendVarsel(varseltype, veileder);
            arbeidsgiverVarselService.sendVarsel(varseltype, mote);
            sykmeldtVarselService.sendVarsel(varseltype, mote);
        }
        moteDAO.setStatus(mote.id, AVBRUTT.name());
        hendelseService.moteStatusEndret(mote.status(AVBRUTT));
        if (feedService.skalOppretteFeedHendelse(mote, PFeedHendelse.FeedHendelseType.AVBRUTT)) {
            opprettFeedHendelseAvTypen(PFeedHendelse.FeedHendelseType.AVBRUTT, mote);
        }
        metricsService.reportAntallDagerSiden(hentSisteSvartidspunkt(mote).orElse(mote.opprettetTidspunkt), "antallDagerVeilederSvar");
    }

    @Transactional
    public void bekreftMote(String moteUuid, Long tidOgStedId, String userId) {
        Mote mote = moteDAO.findMoteByUUID(moteUuid);

        metricsService.reportAntallDagerSiden(mote.opprettetTidspunkt, "antallDagerForSvar");
        metricsService.reportAntallDagerSiden(hentSisteSvartidspunkt(mote).orElse(mote.opprettetTidspunkt), "antallDagerVeilederSvar");

        moteDAO.bekreftMote(mote.id, tidOgStedId);
        mote.valgtTidOgSted(mote.alternativer.stream().filter(tidOgSted -> tidOgSted.id.equals(tidOgStedId)).findFirst().orElseThrow(() -> new RuntimeException("Fant ikke tidspunktet!")));

        hendelseService.moteStatusEndret(mote.status(BEKREFTET));
        Veileder veileder = veilederService.hentVeileder(userId)
                .mote(mote);

        mqStoppRevarslingService.stoppReVarsel(finnAktoerIMote(mote).uuid);
        veilederVarselService.sendVarsel(Varseltype.BEKREFTET, veileder);
        arbeidsgiverVarselService.sendVarsel(Varseltype.BEKREFTET, mote);
        sykmeldtVarselService.sendVarsel(Varseltype.BEKREFTET, mote);
        if (feedService.skalOppretteFeedHendelse(mote, PFeedHendelse.FeedHendelseType.BEKREFTET)) {
            opprettFeedHendelseAvTypen(PFeedHendelse.FeedHendelseType.BEKREFTET, mote);
        }
    }

    @Transactional
    public void nyeAlternativer(String moteUuid, List<TidOgSted> nyeAlternativer, String userId) {
        Mote mote = moteDAO.findMoteByUUID(moteUuid);

        List<TidOgSted> filtrerBortAlternativerSomAlleredeErLagret = filtrerBortAlternativerSomAlleredeErLagret(nyeAlternativer, mote);
        if (filtrerBortAlternativerSomAlleredeErLagret.isEmpty()) {
            return;
        }

        hendelseService.moteStatusEndret(mote.status(FLERE_TIDSPUNKT));
        nyeAlternativer.forEach(tidOgSted -> tidOgStedDAO.create(tidOgSted.moteId(mote.id)));
        Veileder veileder = veilederService.hentVeileder(userId).mote(mote);

        veilederVarselService.sendVarsel(Varseltype.NYE_TIDSPUNKT, veileder);
        arbeidsgiverVarselService.sendVarsel(Varseltype.NYE_TIDSPUNKT, mote);
        sykmeldtVarselService.sendVarsel(Varseltype.NYE_TIDSPUNKT, mote);
        if (feedService.skalOppretteFeedHendelse(mote, PFeedHendelse.FeedHendelseType.FLERE_TIDSPUNKT)) {
            opprettFeedHendelseAvTypen(PFeedHendelse.FeedHendelseType.FLERE_TIDSPUNKT, mote);
        }
        metricsService.reportAntallDagerSiden(hentSisteSvartidspunkt(mote).orElse(mote.opprettetTidspunkt), "antallDagerVeilederSvar");
    }

    public void overforMoteTil(String moteUuid, String mottakerUserId) {
        Mote mote = moteDAO.findMoteByUUID(moteUuid);
        List<Enhet> mottakerEnheter = norgService.hentVeiledersNavEnheter(mottakerUserId);

        boolean mottakerHarIkkeTilgangTilOppgittNavEnhet = mottakerEnheter.stream().noneMatch(enhet -> mote.navEnhet.equals(enhet.enhetId));
        if (mottakerHarIkkeTilgangTilOppgittNavEnhet) {
            throw new ForbiddenException();
        }

        moteDAO.oppdaterMoteEier(moteUuid, mottakerUserId);
    }


    public boolean harAlleSvartPaaSisteForespoersel(Mote mote) {
        boolean skalSykmeldtHaVarsler = dkifService.hentKontaktinfoAktoerId(mote.sykmeldt().aktorId).skalHaVarsel;
        LocalDateTime nyesteAlternativOpprettetTidspunkt = mote.alternativer.stream().sorted((o1, o2) -> o2.created.compareTo(o1.created)).findFirst().get().created;

        return mote.motedeltakere.stream()
                .filter(erIkkeReservertSykmeldt(skalSykmeldtHaVarsler))
                .noneMatch(deltaker -> deltaker.svartTidspunkt == null ||
                        deltaker.svartTidspunkt.isBefore(nyesteAlternativOpprettetTidspunkt) ||
                        sisteSvarErIkkeReservertBruk(mote, skalSykmeldtHaVarsler));
    }

    private boolean sisteSvarErIkkeReservertBruk(Mote mote, boolean skalSykmeldtHaVarsler) {
        return !skalSykmeldtHaVarsler &&
                mote.motedeltakere.stream()
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
            metricsService.reportAntallDagerSiden(mote.opprettetTidspunkt, "antallDagerBrukerSvar");
        } else if ("arbeidsgiver".equals(brukerSomSvarte.motedeltakertype)) {
            metricsService.reportAntallDagerSiden(mote.opprettetTidspunkt, "antallDagerArbeidsgiverSvar");
        }

        if (harAlleSvartPaaSisteForespoersel(mote)) {
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

    private void opprettFeedHendelseAvTypen(PFeedHendelse.FeedHendelseType type, Mote mote) {
        feedDAO.createFeedHendelse(new PFeedHendelse()
                .sistEndretAv(getUserId())
                .uuid(feedService.finnNyesteFeedUuidiMote(mote))
                .type(type.name())
                .moteId(mote.id)
        );
    }
}

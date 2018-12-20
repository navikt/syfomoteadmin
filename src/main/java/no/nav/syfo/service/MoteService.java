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
            arbeidsgiverVarselService.sendVarsel(varseltype, Mote);
            sykmeldtVarselService.sendVarsel(varseltype, Mote);
        }
        moteDAO.setStatus(Mote.id, AVBRUTT.name());
        hendelseService.moteStatusEndret(Mote.status(AVBRUTT));
        if (feedService.skalOppretteFeedHendelse(Mote, PFeedHendelse.FeedHendelseType.AVBRUTT)) {
            opprettFeedHendelseAvTypen(PFeedHendelse.FeedHendelseType.AVBRUTT, Mote);
        }
        metricsService.reportAntallDagerSiden(hentSisteSvartidspunkt(Mote).orElse(Mote.opprettetTidspunkt), "antallDagerVeilederSvar");
    }

    @Transactional
    public void bekreftMote(String moteUuid, Long tidOgStedId, String userId) {
        Mote Mote = moteDAO.findMoteByUUID(moteUuid);

        metricsService.reportAntallDagerSiden(Mote.opprettetTidspunkt, "antallDagerForSvar");
        metricsService.reportAntallDagerSiden(hentSisteSvartidspunkt(Mote).orElse(Mote.opprettetTidspunkt), "antallDagerVeilederSvar");

        moteDAO.bekreftMote(Mote.id, tidOgStedId);
        Mote.valgtTidOgSted(Mote.alternativer.stream().filter(tidOgSted -> tidOgSted.id.equals(tidOgStedId)).findFirst().orElseThrow(() -> new RuntimeException("Fant ikke tidspunktet!")));

        hendelseService.moteStatusEndret(Mote.status(BEKREFTET));
        Veileder veileder = veilederService.hentVeileder(userId)
                .mote(Mote);

        mqStoppRevarslingService.stoppReVarsel(finnAktoerIMote(Mote).uuid);
        veilederVarselService.sendVarsel(Varseltype.BEKREFTET, veileder);
        arbeidsgiverVarselService.sendVarsel(Varseltype.BEKREFTET, Mote);
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
        arbeidsgiverVarselService.sendVarsel(Varseltype.NYE_TIDSPUNKT, Mote);
        sykmeldtVarselService.sendVarsel(Varseltype.NYE_TIDSPUNKT, Mote);
        if (feedService.skalOppretteFeedHendelse(Mote, PFeedHendelse.FeedHendelseType.FLERE_TIDSPUNKT)) {
            opprettFeedHendelseAvTypen(PFeedHendelse.FeedHendelseType.FLERE_TIDSPUNKT, Mote);
        }
        metricsService.reportAntallDagerSiden(hentSisteSvartidspunkt(Mote).orElse(Mote.opprettetTidspunkt), "antallDagerVeilederSvar");
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


    public boolean harAlleSvartPaaSisteForespoersel(Mote Mote) {
        boolean skalSykmeldtHaVarsler = dkifService.hentKontaktinfoAktoerId(Mote.sykmeldt().aktorId).skalHaVarsel;
        LocalDateTime nyesteAlternativOpprettetTidspunkt = Mote.alternativer.stream().sorted((o1, o2) -> o2.created.compareTo(o1.created)).findFirst().get().created;

        return Mote.motedeltakere.stream()
                .filter(erIkkeReservertSykmeldt(skalSykmeldtHaVarsler))
                .noneMatch(deltaker -> deltaker.svartTidspunkt == null ||
                        deltaker.svartTidspunkt.isBefore(nyesteAlternativOpprettetTidspunkt) ||
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

    public void svarMottatt(String motedeltakerSomSvarteUuid, Mote Mote) {
        Motedeltaker brukerSomSvarte = Mote.motedeltakere.stream()
                .filter(deltaker -> deltaker.uuid.equals(motedeltakerSomSvarteUuid))
                .findFirst().get();
        Mote.motedeltakere = Mote.motedeltakere.stream()
                .map(motedeltaker -> motedeltaker.uuid.equals(brukerSomSvarte.uuid) ? motedeltaker.svartTidspunkt(now()) : motedeltaker)
                .collect(toList());

        if ("Bruker".equals(brukerSomSvarte.motedeltakertype)) {
            mqStoppRevarslingService.stoppReVarsel(brukerSomSvarte.uuid);
            metricsService.reportAntallDagerSiden(Mote.opprettetTidspunkt, "antallDagerBrukerSvar");
        } else if ("arbeidsgiver".equals(brukerSomSvarte.motedeltakertype)) {
            metricsService.reportAntallDagerSiden(Mote.opprettetTidspunkt, "antallDagerArbeidsgiverSvar");
        }

        if (harAlleSvartPaaSisteForespoersel(Mote)) {
            feedDAO.createFeedHendelse(new PFeedHendelse()
                    .sistEndretAv(Mote.eier)
                    .type(ALLE_SVAR_MOTTATT.name())
                    .uuid(UUID.randomUUID().toString())
                    .moteId(Mote.id)
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
                .sistEndretAv(getUserId())
                .uuid(feedService.finnNyesteFeedUuidiMote(Mote))
                .type(type.name())
                .moteId(Mote.id)
        );
    }
}

package no.nav.syfo.service;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.domain.AktorId;
import no.nav.syfo.domain.Fodselsnummer;
import no.nav.syfo.api.domain.bruker.*;
import no.nav.syfo.domain.model.*;
import no.nav.syfo.api.exception.ConflictException;
import no.nav.syfo.consumer.pdl.PdlConsumer;
import no.nav.syfo.domain.Brukerkontekst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static no.nav.syfo.api.mappers.BrukerMoteMapper.mote2BrukerMote;
import static no.nav.syfo.domain.model.MoteStatus.*;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.api.auth.OIDCUtil.getSubjectEkstern;

@Service
public class MoteBrukerService {
    private static final Logger log = LoggerFactory.getLogger(MoteBrukerService.class);

    private OIDCRequestContextHolder contextHolder;

    private final PdlConsumer pdlConsumer;

    private BrukertilgangService brukertilgangService;

    private MoteService moteService;

    private MotedeltakerService motedeltakerService;

    private NaermesteLedersMoterService naermesteLedersMoterService;

    @Inject
    public MoteBrukerService(
            OIDCRequestContextHolder contextHolder,
            BrukertilgangService brukertilgangService,
            MoteService moteService,
            MotedeltakerService motedeltakerService,
            NaermesteLedersMoterService naermesteLedersMoterService,
            PdlConsumer pdlConsumer
    ) {
        this.contextHolder = contextHolder;
        this.brukertilgangService = brukertilgangService;
        this.moteService = moteService;
        this.motedeltakerService = motedeltakerService;
        this.naermesteLedersMoterService = naermesteLedersMoterService;
        this.pdlConsumer = pdlConsumer;
    }


    public BrukerMote hentSisteBrukerMote(String aktoerId, String brukerkontekst) {
        return hentBrukerMoteListe(aktoerId, brukerkontekst)
                .stream()
                .min((o1, o2) -> o2.opprettetTidspunkt.compareTo(o1.opprettetTidspunkt))
                .orElseThrow(() -> new NotFoundException("Fant ingen møter på brukeren"));
    }

    public Optional<BrukerMote> hentSisteBrukerMoteEtterDato(String aktorId, String brukerkontekst, LocalDateTime dato) {
        return Optional.of(hentBrukerMoteListe(aktorId, brukerkontekst)
                .stream()
                .filter((mote) -> mote.opprettetTidspunkt.isAfter(dato))
                .min((o1, o2) -> o2.opprettetTidspunkt.compareTo(o1.opprettetTidspunkt)))
                .orElse(Optional.empty());
    }

    public Boolean harMoteplanleggerIBruk(Fodselsnummer fnr, String brukerkontekst, LocalDateTime tidligsteOpprettetGrense) {
        AktorId aktorId = pdlConsumer.aktorId(fnr);
        LocalDateTime today = LocalDateTime.now();
        return Optional.of(hentBrukerMoteListe(aktorId.getValue(), brukerkontekst)
                .stream()
                .filter((mote) ->
                        mote.status.equals(OPPRETTET.name())
                                || mote.status.equals(FLERE_TIDSPUNKT.name())
                                || (mote.status.equals(BEKREFTET.name()) && today.isBefore(mote.bekreftetAlternativ.tid.plusDays(1)))
                )
                .filter((mote) -> mote.opprettetTidspunkt.isAfter(tidligsteOpprettetGrense))
                .min((o1, o2) -> o2.opprettetTidspunkt.compareTo(o1.opprettetTidspunkt)))
                .orElse(Optional.empty()).isPresent();
    }

    public List<BrukerMote> hentBrukerMoteListe(String aktorId, String brukerkontekst) {
        List<Mote> moter = hentMoteListe(aktorId, brukerkontekst);

        return mapListe(moter, mote2BrukerMote)
                .stream()
                .map(brukerMote -> brukerMote
                        .fnr(finnAktoersFnrFraMotet(brukerMote).getValue())
                        .deltakere(brukerMote.deltakere()
                                .stream()
                                .map(deltaker -> {
                                    if (Brukerkontekst.ARBEIDSTAKER.equals(deltaker.type)) {
                                        return deltaker.navn(pdlConsumer.fullName(pdlConsumer.fodselsnummer(new AktorId(deltaker.aktoerId)).getValue()));
                                    }
                                    return deltaker;
                                })
                                .collect(toList())))

                .collect(toList());
    }

    public BrukerOppdaterMoteSvar sendSvar(String moteUuid, BrukerMoteSvar brukerMoteSvar) {
        String brukerkontekst = Brukerkontekst.ARBEIDSTAKER.equals(brukerMoteSvar.deltakertype)
                ? Brukerkontekst.ARBEIDSTAKER
                : Brukerkontekst.ARBEIDSGIVER;
        AktorId innloggetAktorId = pdlConsumer.aktorId(new Fodselsnummer(getSubjectEkstern(contextHolder)));
        Mote mote = hentMoteByUuid(moteUuid, innloggetAktorId.getValue(), brukerkontekst);
        String arbeidstakerAktorId = motedeltakerService.finnArbeidstakerAktorIdForMoteId(mote.id);
        Fodselsnummer arbeidstakerFnr = pdlConsumer.fodselsnummer(new AktorId(arbeidstakerAktorId));

        brukertilgangService.kastExceptionHvisIkkeTilgang(arbeidstakerFnr.getValue());

        if (mote.status.equals(MoteStatus.AVBRUTT)) {
            throw new IllegalStateException("Prøver å svare på et alternativ som ikke har status OPPRETTET");
        }

        Motedeltaker motedeltaker = mote.motedeltakere.stream()
                .filter(deltaker -> brukerMoteSvar.deltakertype.equals(deltaker.motedeltakertype))
                .findFirst().orElseThrow(NotFoundException::new);

        if (harSvartEtterSisteAlternativBleOpprettet(motedeltaker)) {
            throw new ConflictException();
        }

        String motedeltakerUuid = motedeltaker.uuid;

        List<BrukerTidOgSted> brukerTidOgStedListe = mapListe(brukerMoteSvar.valgteAlternativIder, id -> new BrukerTidOgSted().id(id));

        motedeltakerService.deltakerHarSvart(motedeltakerUuid, mapListe(brukerTidOgStedListe, BrukerTidOgSted::id));
        moteService.svarMottatt(motedeltakerUuid, mote);

        return new BrukerOppdaterMoteSvar();
    }

    private List<Mote> hentMoteListe(String aktorId, String brukerkontekst) {
        if (Brukerkontekst.ARBEIDSGIVER.equals(brukerkontekst)) {
            return naermesteLedersMoterService.hentNarmesteLedersMoter(aktorId);
        } else if (Brukerkontekst.ARBEIDSTAKER.equals(brukerkontekst)) {
            return moteService.findMoterByBrukerAktoerId(aktorId);
        } else {
            log.error("Ukjent brukerkontekst " + brukerkontekst);
            throw new RuntimeException("Ukjent brukerkontekst " + brukerkontekst);
        }
    }

    private Mote hentMoteByUuid(String moteUuid, String aktoerId, String brukerkontekst) {
        return hentMoteListe(aktoerId, brukerkontekst)
                .stream()
                .filter(brukerMote -> brukerMote.uuid.equals(moteUuid))
                .findFirst().orElseThrow(() -> new NotFoundException("Fant ikke møtet på brukeren"));
    }

    private static boolean harSvartEtterSisteAlternativBleOpprettet(Motedeltaker motedeltaker) {
        return motedeltaker.tidOgStedAlternativer
                .stream()
                .noneMatch(alternativ -> motedeltaker.svartTidspunkt == null || alternativ.created.isAfter(motedeltaker.svartTidspunkt));
    }

    private Fodselsnummer finnAktoersFnrFraMotet(BrukerMote mote) {
        String aktorId = mote.deltakere.stream()
                .filter(motedeltaker -> "Bruker".equals(motedeltaker.type))
                .findFirst().orElseThrow(() -> new NotFoundException("Fant ikke bruker!"))
                .aktoerId;
        return pdlConsumer.fodselsnummer(new AktorId(aktorId));
    }
}

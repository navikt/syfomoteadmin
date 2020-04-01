package no.nav.syfo.service;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.aktorregister.domain.*;
import no.nav.syfo.api.domain.bruker.*;
import no.nav.syfo.domain.model.*;
import no.nav.syfo.exception.ConflictException;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.util.Brukerkontekst;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static no.nav.syfo.api.mappers.BrukerMoteMapper.mote2BrukerMote;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.OIDCUtil.getSubjectEkstern;

@Service
public class MoteBrukerService {
    private static final Logger log = LoggerFactory.getLogger(MoteBrukerService.class);

    private OIDCRequestContextHolder contextHolder;

    private AktorregisterConsumer aktorregisterConsumer;

    private final PdlConsumer pdlConsumer;

    private BrukertilgangService brukertilgangService;

    private MoteService moteService;

    private MotedeltakerService motedeltakerService;

    private NaermesteLedersMoterService naermesteLedersMoterService;

    @Inject
    public MoteBrukerService(
            OIDCRequestContextHolder contextHolder,
            AktorregisterConsumer aktorregisterConsumer,
            BrukertilgangService brukertilgangService,
            MoteService moteService,
            MotedeltakerService motedeltakerService,
            NaermesteLedersMoterService naermesteLedersMoterService,
            PdlConsumer pdlConsumer
    ) {
        this.contextHolder = contextHolder;
        this.aktorregisterConsumer = aktorregisterConsumer;
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

    public List<BrukerMote> hentBrukerMoteListe(String aktorId, String brukerkontekst) {
        List<Mote> moter = hentMoteListe(aktorId, brukerkontekst);

        return mapListe(moter, mote2BrukerMote)
                .stream()
                .map(brukerMote -> brukerMote
                        .fnr(finnAktoersFnrFraMotet(brukerMote))
                        .deltakere(brukerMote.deltakere()
                                .stream()
                                .map(deltaker -> {
                                    if (Brukerkontekst.ARBEIDSTAKER.equals(deltaker.type)) {
                                        return deltaker.navn(pdlConsumer.fullName(aktorregisterConsumer.getFnrForAktorId(new AktorId(deltaker.aktoerId))));
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
        String innloggetAktorId = aktorregisterConsumer.getAktorIdForFodselsnummer(new Fodselsnummer(getSubjectEkstern(contextHolder)));
        Mote mote = hentMoteByUuid(moteUuid, innloggetAktorId, brukerkontekst);
        String arbeidstakerAktorId = motedeltakerService.finnArbeidstakerAktorIdForMoteId(mote.id);
        String arbeidstakerFnr = aktorregisterConsumer.getFnrForAktorId(new AktorId(arbeidstakerAktorId));

        brukertilgangService.kastExceptionHvisIkkeTilgang(arbeidstakerFnr);

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
            return naermesteLedersMoterService.hentNaermesteLedersMoter(aktorId);
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

    private String finnAktoersFnrFraMotet(BrukerMote mote) {
        String aktorId = mote.deltakere.stream()
                .filter(motedeltaker -> "Bruker".equals(motedeltaker.type))
                .findFirst().orElseThrow(() -> new NotFoundException("Fant ikke bruker!"))
                .aktoerId;
        return aktorregisterConsumer.getFnrForAktorId(new AktorId(aktorId));
    }
}

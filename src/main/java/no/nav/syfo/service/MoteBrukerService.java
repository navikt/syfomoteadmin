package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.api.domain.bruker.BrukerMote;
import no.nav.syfo.api.domain.bruker.BrukerMoteSvar;
import no.nav.syfo.api.domain.bruker.BrukerOppdaterMoteSvar;
import no.nav.syfo.api.domain.bruker.BrukerTidOgSted;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.MoteStatus;
import no.nav.syfo.domain.model.Motedeltaker;
import no.nav.syfo.util.Brukerkontekst;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.nav.syfo.api.mappers.BrukerMoteMapper.mote2BrukerMote;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.OIDCUtil.getSubjectEkstern;

@Slf4j
@Service
public class MoteBrukerService {

    private OIDCRequestContextHolder contextHolder;

    private AktoerService aktoerService;

    private BrukerprofilService brukerprofilService;

    private BrukertilgangService brukertilgangService;

    private MoteService moteService;

    private MotedeltakerService motedeltakerService;

    private NaermesteLedersMoterService naermesteLedersMoterService;

    @Inject
    public MoteBrukerService(
            OIDCRequestContextHolder contextHolder,
            AktoerService aktoerService,
            BrukerprofilService brukerprofilService,
            BrukertilgangService brukertilgangService,
            MoteService moteService,
            MotedeltakerService motedeltakerService,
            NaermesteLedersMoterService naermesteLedersMoterService
    ) {
        this.contextHolder = contextHolder;
        this.aktoerService = aktoerService;
        this.brukerprofilService = brukerprofilService;
        this.brukertilgangService = brukertilgangService;
        this.moteService = moteService;
        this.motedeltakerService = motedeltakerService;
        this.naermesteLedersMoterService = naermesteLedersMoterService;
    }


    public BrukerMote hentSisteBrukerMote(String aktoerId, String brukerkontekst) {
        return hentBrukerMoteListe(aktoerId, brukerkontekst)
                .stream()
                .min((o1, o2) -> o2.opprettetTidspunkt.compareTo(o1.opprettetTidspunkt))
                .orElseThrow(() -> new NotFoundException("Fant ingen møter på brukeren"));
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
                                        return deltaker.navn(brukerprofilService.finnBrukerPersonnavnByAktoerId(deltaker.aktoerId));
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
        String innloggetAktorId = aktoerService.hentAktoerIdForIdent(getSubjectEkstern(contextHolder));
        Mote mote = hentMoteByUuid(moteUuid, innloggetAktorId, brukerkontekst);
        String arbeidstakerAktorId = motedeltakerService.finnArbeidstakerAktorIdForMoteId(mote.id);
        String arbeidstakerFnr = aktoerService.hentFnrForAktoer(arbeidstakerAktorId);

        brukertilgangService.kastExceptionHvisIkkeTilgang(arbeidstakerFnr);

        if (mote.status.equals(MoteStatus.AVBRUTT)) {
            throw new IllegalStateException("Prøver å svare på et alternativ som ikke har status OPPRETTET");
        }

        Motedeltaker motedeltaker = mote.motedeltakere.stream()
                .filter(deltaker -> brukerMoteSvar.deltakertype.equals(deltaker.motedeltakertype))
                .findFirst().orElseThrow(NotFoundException::new);

        if (harSvartEtterSisteAlternativBleOpprettet(motedeltaker)) {
            throw new ClientErrorException(409);
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
        return aktoerService.hentFnrForAktoer(mote.deltakere.stream()
                .filter(motedeltaker -> "Bruker".equals(motedeltaker.type))
                .findFirst().orElseThrow(() -> new NotFoundException("Fant ikke bruker!"))
                .aktoerId);
    }
}

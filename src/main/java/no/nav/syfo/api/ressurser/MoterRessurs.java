package no.nav.syfo.api.ressurser;

import no.nav.metrics.aspects.Count;
import no.nav.metrics.aspects.Timed;
import no.nav.syfo.domain.model.*;
import no.nav.syfo.repository.dao.MotedeltakerDAO;
import no.nav.syfo.repository.dao.TidOgStedDAO;
import no.nav.syfo.repository.model.PMotedeltakerAktorId;
import no.nav.syfo.repository.model.PMotedeltakerArbeidsgiver;
import no.nav.syfo.api.domain.RSMote;
import no.nav.syfo.api.domain.RSTilgang;
import no.nav.syfo.api.domain.nyttmoterequest.RSNyttMoteRequest;
import no.nav.syfo.service.TilgangService;
import no.nav.syfo.service.*;
import no.nav.syfo.service.varselinnhold.ArbeidsgiverVarselService;
import no.nav.syfo.service.varselinnhold.SykmeldtVarselService;
import no.nav.syfo.service.varselinnhold.VeilederVarselService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.status;
import static no.nav.syfo.domain.model.MotedeltakerStatus.SENDT;
import static no.nav.syfo.domain.model.Varseltype.OPPRETTET;
import static no.nav.syfo.api.mappers.RSMoteMapper.mote2rs;
import static no.nav.syfo.api.mappers.RSNyttMoteMapper.opprett2Mote;
import static no.nav.syfo.api.mappers.RSNyttMoteMapper.opprett2TidOgSted;
import static no.nav.syfo.util.MapUtil.map;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.SubjectHandlerUtil.getUserId;
import static org.springframework.util.StringUtils.isEmpty;

@Component
@Path("/moter")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class MoterRessurs {

    @Inject
    private AktoerService aktoerService;
    @Inject
    private MoteService moteService;
    @Inject
    private TidOgStedDAO tidOgStedDAO;
    @Inject
    private HendelseService hendelseService;
    @Inject
    private MotedeltakerDAO motedeltakerDAO;
    @Inject
    private NorgService norgService;
    @Inject
    private BrukerprofilService brukerprofilService;
    @Inject
    private VeilederService veilederService;
    @Inject
    private VeilederVarselService veilederVarselService;
    @Inject
    private ArbeidsgiverVarselService arbeidsgiverVarselService;
    @Inject
    private SykefravaersoppfoelgingService sykefravaersoppfoelgingService;
    @Inject
    private SykmeldtVarselService sykmeldtVarselService;
    @Inject
    private TilgangService tilgangService;

    /**
     * @deprecated To complex - needs to be split in at least to methods.
     * See jira task SYFOUTV-2166
     */
    @GET
    @Timed(name = "getMoter")
    @Count(name = "getMoter")
    @Deprecated
    public List<RSMote> hentMoter(@QueryParam("limit") Integer limit,
                                  @QueryParam("fnr") String fnr,
                                  @QueryParam("veiledersmoter") Boolean veiledersMoter,
                                  @QueryParam("navenhet") String navenhet,
                                  @QueryParam("henttpsdata") boolean hentTpsData) {

        List<Mote> moter = new ArrayList<>();
        List<Mote> moterByFnr = new ArrayList<>();

        if (!isEmpty(fnr)) {
            boolean erBrukerSkjermet = brukerprofilService.hentBruker(fnr).skjermetBruker;
            Response tilgangResponse = tilgangService.sjekkTilgangTilPerson(fnr);
            if (erBrukerSkjermet || tilgangResponse.getStatus() == 403) {
                throw new ForbiddenException(status(FORBIDDEN)
                        .entity(erBrukerSkjermet ?
                                new RSTilgang()
                                        .harTilgang(false)
                                        .begrunnelse("KODE7")
                                : tilgangResponse.getEntity())
                        .type(APPLICATION_JSON)
                        .build());
            } else {
                moterByFnr.addAll(moteService.findMoterByBrukerAktoerId(aktoerService.hentAktoerIdForIdent(fnr)));
                moter.addAll(moterByFnr);
            }
        }

        List<Mote> moterByVeileder = new ArrayList<>();
        if (!isEmpty(veiledersMoter) && veiledersMoter) {
            moterByVeileder.addAll(moteService.findMoterByBrukerNavAnsatt(getUserId()));
            moter.addAll(moterByVeileder);
        }

        List<Mote> moterByNavEnhet = new ArrayList<>();
        if (!isEmpty(navenhet) && norgService.hoererNavEnhetTilBruker(navenhet, getUserId())) {
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
                .filter(mote -> !brukerprofilService.hentBruker(aktoerService.hentFnrForAktoer(mote.sykmeldt().aktorId)).skjermetBruker)
                .filter(mote -> tilgangService.sjekkTilgangTilPerson(aktoerService.hentFnrForAktoer(mote.sykmeldt().aktorId)).getStatus() == 200)
                .collect(toList());

        if (limit != null) {
            moter = moter.stream()
                    .sorted((o1, o2) -> o2.opprettetTidspunkt.compareTo(o1.opprettetTidspunkt))
                    .limit(limit).collect(toList());
        }
        if (hentTpsData) {
            moter = populerMedTpsData(moter);
        }

        return mapListe(moter, mote2rs).stream().map(rsMote -> rsMote.sistEndret(hendelseService.sistEndretMoteStatus(rsMote.id).orElse(rsMote.opprettetTidspunkt))).collect(toList());
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

    @POST
    @Timed(name = "opprettetMote")
    @Count(name = "opprettetMote")
    public void opprett(RSNyttMoteRequest nyttMoteRequest) {
        if (brukerprofilService.hentBruker(nyttMoteRequest.fnr).skjermetBruker || tilgangService.sjekkTilgangTilPerson(nyttMoteRequest.fnr).getStatus() == 403) {
            throw new ForbiddenException();
        } else {
            String aktorId = aktoerService.hentAktoerIdForIdent(nyttMoteRequest.fnr);
            NaermesteLeder naermesteLeder = sykefravaersoppfoelgingService.hentNaermesteLederSomBruker(aktorId, nyttMoteRequest.orgnummer);
            nyttMoteRequest.navn(naermesteLeder.navn);
            nyttMoteRequest.epost(naermesteLeder.epost);

            Mote Mote = moteService.opprettMote(map(nyttMoteRequest, opprett2Mote));
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

            Veileder veileder = veilederService.hentVeileder(getUserId())
                    .mote(Mote);
            veilederVarselService.sendVarsel(OPPRETTET, veileder);
            arbeidsgiverVarselService.sendVarsel(OPPRETTET, Mote, false);
            sykmeldtVarselService.sendVarsel(OPPRETTET, Mote);
        }
    }

    private List<Mote> intersection(List<Mote> liste1, List<Mote> liste2) {
        return liste2.stream()
                .filter(rsMote1 -> liste1.stream()
                        .anyMatch(rsMote2 -> rsMote2.id.equals(rsMote1.id)))
                .collect(toList());
    }

}

package no.nav.syfo.service.varselinnhold;

import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.NaermesteLeder;
import no.nav.syfo.domain.model.TidOgSted;
import no.nav.syfo.domain.model.Varseltype;
import no.nav.syfo.repository.model.PEpost;
import no.nav.syfo.service.EpostService;
import no.nav.syfo.service.HendelseService;
import no.nav.syfo.service.SykefravaersoppfoelgingService;
import no.nav.syfo.service.VeilederService;

import javax.inject.Inject;

import static java.lang.System.getProperty;
import static no.nav.syfo.util.time.DateUtil.tilKortDato;
import static no.nav.syfo.domain.model.Varseltype.*;
import static no.nav.syfo.util.EpostInnholdUtil.*;
import static no.nav.syfo.util.SubjectHandlerUtil.getUserId;

public class ArbeidsgiverVarselService {

    @Inject
    private VeilederService veilederService;
    @Inject
    private EpostService epostService;
    @Inject
    private HendelseService hendelseService;
    @Inject
    private SykefravaersoppfoelgingService sykefravaersoppfoelgingService;

    public void sendVarsel(Varseltype varseltype, Mote Mote, boolean erSystemKall) {
        hendelseService.opprettHendelseVarselArbeidsgiver(varseltype, Mote.arbeidsgiver());
        epostService.klargjorForSending(varselinnhold(varseltype, Mote, erSystemKall));
    }

    public PEpost varselinnhold(Varseltype varseltype, Mote Mote, boolean erSystemKall) {
        if (varseltype == OPPRETTET) {
            return arbeidsgiverNyttMote(Mote.arbeidsgiver().navn, finnLenkeUrl(Mote.sykmeldt().aktorId, Mote.arbeidsgiver().orgnummer,erSystemKall), veiledernavn()).mottaker(Mote.arbeidsgiver().epost);
        } else if (varseltype == AVBRUTT_BEKREFTET) {
            return arbeidsgiverAvbrytBekreftetMote(Mote.arbeidsgiver().navn, veiledernavn(), Mote).mottaker(Mote.arbeidsgiver().epost);
        } else if (varseltype == AVBRUTT) {
            return arbeidsgiverAvbrytMote(Mote.arbeidsgiver().navn, veiledernavn(), Mote).mottaker(Mote.arbeidsgiver().epost);
        } else if (varseltype == BEKREFTET) {
            return bekreftelseEpost(Mote.arbeidsgiver().navn, Mote.arbeidsgiver().uuid, sted(Mote.valgtTidOgSted), Mote.valgtTidOgSted.tid, veiledernavn()).mottaker(Mote.arbeidsgiver().epost);
        } else if (varseltype == NYE_TIDSPUNKT) {
            return arbeidsgiverNyeTidspunkt(Mote.arbeidsgiver().navn, finnLenkeUrl(Mote.sykmeldt().aktorId, Mote.arbeidsgiver().orgnummer, erSystemKall), veiledernavn()).mottaker(Mote.arbeidsgiver().epost);
        } else if (varseltype == PAAMINNELSE) {
            return arbeidsgiverPaaminnelseMote(Mote.arbeidsgiver().navn, "NAV", finnLenkeUrl(Mote.sykmeldt().aktorId, Mote.arbeidsgiver().orgnummer, erSystemKall), opprettetTidspunkt(Mote))
                    .mottaker(Mote.arbeidsgiver().epost);
        }
        return null;
    }

    private String veiledernavn() {
        return veilederService.hentVeileder(getUserId()).navn;
    }

    private String sted(TidOgSted valgtTidOgSted) {
        return valgtTidOgSted.sted;
    }

    private String opprettetTidspunkt(Mote Mote) {
        return tilKortDato(Mote.opprettetTidspunkt);
    }

    private String finnLenkeUrl(String aktorId, String orgnummer, boolean erSystemKall) {
        NaermesteLeder naermesteLeder;
        if (erSystemKall) {
            naermesteLeder = sykefravaersoppfoelgingService.hentNaermesteLederSomSystembruker(aktorId, orgnummer);
        } else {
            naermesteLeder = sykefravaersoppfoelgingService.hentNaermesteLederSomBruker(aktorId, orgnummer);
        }
        return getProperty("TJENESTER_URL") + "/sykefravaerarbeidsgiver/" + naermesteLeder.naermesteLederId + "/dialogmoter";
    }
}

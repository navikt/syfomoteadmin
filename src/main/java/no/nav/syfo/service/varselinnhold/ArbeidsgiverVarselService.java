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

    public void sendVarsel(Varseltype varseltype, Mote mote) {
        hendelseService.opprettHendelseVarselArbeidsgiver(varseltype, mote.arbeidsgiver());
        epostService.klargjorForSending(varselinnhold(varseltype, mote));
    }

    public PEpost varselinnhold(Varseltype varseltype, Mote mote) {
        if (varseltype == OPPRETTET) {
            return arbeidsgiverNyttMote(mote.arbeidsgiver().navn, finnLenkeUrl(mote.sykmeldt().aktorId, mote.arbeidsgiver().orgnummer), veiledernavn()).mottaker(mote.arbeidsgiver().epost);
        } else if (varseltype == AVBRUTT_BEKREFTET) {
            return arbeidsgiverAvbrytBekreftetMote(mote.arbeidsgiver().navn, veiledernavn(), mote).mottaker(mote.arbeidsgiver().epost);
        } else if (varseltype == AVBRUTT) {
            return arbeidsgiverAvbrytMote(mote.arbeidsgiver().navn, veiledernavn(), mote).mottaker(mote.arbeidsgiver().epost);
        } else if (varseltype == BEKREFTET) {
            return bekreftelseEpost(mote.arbeidsgiver().navn, mote.arbeidsgiver().uuid, sted(mote.valgtTidOgSted), mote.valgtTidOgSted.tid, veiledernavn()).mottaker(mote.arbeidsgiver().epost);
        } else if (varseltype == NYE_TIDSPUNKT) {
            return arbeidsgiverNyeTidspunkt(mote.arbeidsgiver().navn, finnLenkeUrl(mote.sykmeldt().aktorId, mote.arbeidsgiver().orgnummer), veiledernavn()).mottaker(mote.arbeidsgiver().epost);
        } else if (varseltype == PAAMINNELSE) {
            return arbeidsgiverPaaminnelseMote(mote.arbeidsgiver().navn, "NAV", finnLenkeUrl(mote.sykmeldt().aktorId, mote.arbeidsgiver().orgnummer), opprettetTidspunkt(mote))
                    .mottaker(mote.arbeidsgiver().epost);
        }
        return null;
    }

    private String veiledernavn() {
        return veilederService.hentVeileder(getUserId()).navn;
    }

    private String sted(TidOgSted valgtTidOgSted) {
        return valgtTidOgSted.sted;
    }

    private String opprettetTidspunkt(Mote mote) {
        return tilKortDato(mote.opprettetTidspunkt);
    }

    private String finnLenkeUrl(String aktorId, String orgnummer) {
        NaermesteLeder naermesteLeder = sykefravaersoppfoelgingService.hentNaermesteLeder(aktorId, orgnummer);
        return getProperty("TJENESTER_URL") + "/sykefravaerarbeidsgiver/" + naermesteLeder.naermesteLederId + "/dialogmoter";
    }
}

package no.nav.syfo.service.varselinnhold;

import no.nav.melding.virksomhet.servicemeldingmedkontaktinformasjon.v1.servicemeldingmedkontaktinformasjon.WSParameter;
import no.nav.syfo.domain.model.*;
import no.nav.syfo.narmesteleder.*;
import no.nav.syfo.repository.model.PEpost;
import no.nav.syfo.service.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static no.nav.syfo.domain.model.TredjepartsVarselType.*;
import static no.nav.syfo.domain.model.Varseltype.*;
import static no.nav.syfo.service.varselinnhold.TredjepartsvarselService.createParameter;
import static no.nav.syfo.util.EpostInnholdUtil.*;
import static no.nav.syfo.util.time.DateUtil.tilKortDato;
import static no.nav.syfo.util.time.DateUtil.tilLangDatoMedKlokkeslettPostfixDagPrefix;

@Service
public class ArbeidsgiverVarselService {

    private static final Logger log = LoggerFactory.getLogger(ArbeidsgiverVarselService.class);

    @Value("${tjenester.url}")
    private String tjenesterUrl;

    private VeilederService veilederService;

    private HendelseService hendelseService;

    private NarmesteLederConsumer narmesteLederConsumer;

    private TredjepartsvarselService tredjepartsvarselService;

    @Autowired
    public ArbeidsgiverVarselService(
            VeilederService veilederService,
            HendelseService hendelseService,
            NarmesteLederConsumer narmesteLederConsumer,
            TredjepartsvarselService tredjepartsvarselService
    ) {
        this.veilederService = veilederService;
        this.hendelseService = hendelseService;
        this.narmesteLederConsumer = narmesteLederConsumer;
        this.tredjepartsvarselService = tredjepartsvarselService;
    }

    public void sendVarsel(Varseltype varseltype, Mote Mote, boolean erSystemKall, String innloggetIdent) {
        hendelseService.opprettHendelseVarselArbeidsgiver(varseltype, Mote.arbeidsgiver(), innloggetIdent);
        sendMoteTredjepartsVarsel(varseltype, Mote, erSystemKall, innloggetIdent);
    }


    private void sendMoteTredjepartsVarsel(Varseltype varseltype, Mote mote, boolean erSystemKall, String innloggetIdent) {
        TredjepartsVarselType varselNokkel = null;

        String veiledernavn = erSystemKall ? "NAV" : veiledernavn(innloggetIdent);
        String url = finnLenkeUrlForLeder();

        List<WSParameter> parameterListe = new ArrayList<>();
        parameterListe.add(createParameter("veiledernavn", veiledernavn));
        parameterListe.add(createParameter("navn", mote.arbeidsgiver().navn.trim()));

        if (varseltype == OPPRETTET) {
            varselNokkel = NAERMESTE_LEDER_MOTETIDSPUNKT_OPPRETTET;
            parameterListe.add(createParameter("url", url));
        } else if (varseltype == AVBRUTT_BEKREFTET) {
            varselNokkel = NAERMESTE_LEDER_MOTETIDSPUNKT_AVBRUTT_BEKREFTET;
            parameterListe.add(createParameter("tidspunkt", motetidspunkt(asList(mote.valgtTidOgSted))));
        } else if (varseltype == AVBRUTT) {
            varselNokkel = NAERMESTE_LEDER_MOTETIDSPUNKT_AVBRUTT;
            parameterListe.add(createParameter("tidspunkt", motetidspunkt(mote.alternativer)));
        } else if (varseltype == BEKREFTET) {
            varselNokkel = NAERMESTE_LEDER_MOTETIDSPUNKT_BEKREFTET;
            parameterListe.add(createParameter("url", url));
            parameterListe.add(createParameter("dato", tilLangDatoMedKlokkeslettPostfixDagPrefix(mote.valgtTidOgSted.tid).toLowerCase()));
            parameterListe.add(createParameter("sted", mote.valgtTidOgSted.sted));
        } else if (varseltype == NYE_TIDSPUNKT) {
            varselNokkel = NAERMESTE_LEDER_MOTETIDSPUNKT_NYE_TIDSPUNKT;
            parameterListe.add(createParameter("url", url));
        } else if (varseltype == PAAMINNELSE) {
            varselNokkel = NAERMESTE_LEDER_MOTETIDSPUNKT_PAAMINNELSE;
            parameterListe.add(createParameter("url", url));
        }

        if (ofNullable(varselNokkel).isPresent()) {
            NarmesteLederRelasjon narmesteLederRelasjon = narmesteLederConsumer.narmesteLederRelasjonLeder(mote.sykmeldt().aktorId, mote.arbeidsgiver().orgnummer);
            tredjepartsvarselService.sendVarselTilNaermesteLeder(varselNokkel, narmesteLederRelasjon, parameterListe);
        } else {
            log.error("Fant ikke varselnøkkel for varseltype {}", varseltype);
        }
    }

    public PEpost varselinnhold(Varseltype varseltype, Mote mote, String innloggetIdent) {
        if (varseltype == OPPRETTET) {
            return arbeidsgiverNyttMote(mote.arbeidsgiver().navn, finnLenkeUrlForLeder(), veiledernavn(innloggetIdent)).mottaker(mote.arbeidsgiver().epost);
        } else if (varseltype == AVBRUTT_BEKREFTET) {
            return arbeidsgiverAvbrytBekreftetMote(mote.arbeidsgiver().navn, veiledernavn(innloggetIdent), mote).mottaker(mote.arbeidsgiver().epost);
        } else if (varseltype == AVBRUTT) {
            return arbeidsgiverAvbrytMote(mote.arbeidsgiver().navn, veiledernavn(innloggetIdent), mote).mottaker(mote.arbeidsgiver().epost);
        } else if (varseltype == BEKREFTET) {
            return bekreftelseEpost(mote.arbeidsgiver().navn, finnLenkeUrlForLeder(), sted(mote.valgtTidOgSted), mote.valgtTidOgSted.tid, veiledernavn(innloggetIdent)).mottaker(mote.arbeidsgiver().epost);
        } else if (varseltype == NYE_TIDSPUNKT) {
            return arbeidsgiverNyeTidspunkt(mote.arbeidsgiver().navn, finnLenkeUrlForLeder(), veiledernavn(innloggetIdent)).mottaker(mote.arbeidsgiver().epost);
        } else if (varseltype == PAAMINNELSE) {
            return arbeidsgiverPaaminnelseMote(mote.arbeidsgiver().navn, "NAV", finnLenkeUrlForLeder(), opprettetTidspunkt(mote))
                    .mottaker(mote.arbeidsgiver().epost);
        }
        return null;
    }

    private String veiledernavn(String innloggetIdent) {
        return veilederService.hentVeilederNavn(innloggetIdent).orElse("NAV");
    }

    private String sted(TidOgSted valgtTidOgSted) {
        return valgtTidOgSted.sted;
    }

    private String opprettetTidspunkt(Mote Mote) {
        return tilKortDato(Mote.opprettetTidspunkt);
    }

    private String finnLenkeUrlForLeder() {
        return tjenesterUrl + "/sykefravaerarbeidsgiver/";
    }
}

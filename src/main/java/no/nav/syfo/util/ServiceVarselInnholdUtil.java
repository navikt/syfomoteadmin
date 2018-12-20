package no.nav.syfo.util;

import no.nav.syfo.domain.model.Mote;

import static no.nav.syfo.util.time.DateUtil.tilLangDatoMedKlokkeslettPostfixDagPrefix;

public class ServiceVarselInnholdUtil {

    public static class ServiceVarsel {
        public String emne;
        public String innhold;

        public ServiceVarsel emne(String emne) {
            this.emne = emne;
            return this;
        }

        public ServiceVarsel innhold(String innhold) {
            this.innhold = innhold;
            return this;
        }
    }

    public static ServiceVarsel avbrytEpost() {
        return new ServiceVarsel()
                .emne("Møte med NAV- forespørselen er kansellert")
                .innhold("Du har tidligere mottatt en forespørsel om møte med NAV. Du kan se bort fra forespørselen. Vennlig hilsen NAV.");
    }

    public static ServiceVarsel avbrytBekreftetEpost() {
        return new ServiceVarsel()
                .emne("Møteforespørsel avbrutt")
                .innhold("Du har tidligere mottatt en bekreftelse på et tidspunkt for et dialogmøte med NAV og din arbeidsgiver. " +
                        "Møteforespørselen er avbrutt, og du kan se bort fra denne forespørselen. Er det fortsatt aktuelt med et møte, " +
                        "vil du få en ny forespørsel. Har du spørsmål, kan du kontakte oss på 55 55 33 33. Med vennlig hilsen NAV");
    }

    public static ServiceVarsel bekreftetEpost(Mote Mote) {
        tilLangDatoMedKlokkeslettPostfixDagPrefix(Mote.valgtTidOgSted.tid);
        return new ServiceVarsel()
                .emne("Møtebekreftelse")
                .innhold("Hei! Vi bekrefter møtetidspunkt " + tilLangDatoMedKlokkeslettPostfixDagPrefix(Mote.valgtTidOgSted.tid).toLowerCase() +
                        " Du vil om kort tid få en innkalling i posten med mer informasjon om møtet. Vennlig hilsen NAV");
    }
}

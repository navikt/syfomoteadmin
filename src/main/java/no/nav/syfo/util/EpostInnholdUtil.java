package no.nav.syfo.util;

import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.TidOgSted;
import no.nav.syfo.repository.model.PEpost;
import no.nav.syfo.repository.model.PEpostVedlegg;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static no.nav.syfo.util.time.DateUtil.tilLangDatoMedKlokkeslettPostfixDagPrefix;
import static no.nav.syfo.util.OutlookEventUtil.IcsStatus.TENTATIVE;
import static no.nav.syfo.util.OutlookEventUtil.icsInnhold;

public class EpostInnholdUtil {

    public static PEpost arbeidsgiverPaaminnelseMote(String navn, String veiledernavn, String url, String dato) {
        return new PEpost()
                .emne(arbeidsgiverPaaminnelseMoteEmne())
                .innhold(arbeidsgiverPaaminnelseMoteInnhold(navn, veiledernavn, url, dato));
    }

    private static String arbeidsgiverPaaminnelseMoteEmne() {
        return "Påminnelse om dialogmøte med NAV - hvilke tidspunkt passer?";
    }

    private static String arbeidsgiverPaaminnelseMoteInnhold(String navn, String veiledernavn, String url, String dato) {
        return TOPP +
                "<p>Til " + navn.trim() + ", </p>" +
                "<p>Vi venter på svar fra deg om dialogmøte som vi sendte deg en forespørsel om " + dato + ". " +
                "Du kan klikke på denne <a href=" + url + ">lenken</a> " +
                "og oppgi hvilke tidspunkt som passer. </p>" +

                harDuSporsmal() +
                vennligHilsen(veiledernavn) +
                BUNN;
    }

    private static String harDuSporsmal() {
        return "<p>Har du spørsmål, kan du kontakte oss på 55 55 33 36</p>";
    }

    private static String vennligHilsen(String navn) {
        return "<p>Vennlig hilsen " + navn + "</p>";
    }

    public static PEpost arbeidsgiverNyttMote(String navn, String url, String veiledernavn) {
        return new PEpost()
                .emne(arbeidsgiverNyttMoteEmne())
                .innhold(arbeidsgiverNyttMoteInnhold(navn, url, veiledernavn));
    }

    private static String arbeidsgiverNyttMoteEmne() {
        return "Forespørsel om dialogmøte med NAV";
    }

    private static String arbeidsgiverNyttMoteInnhold(String navn, String url, String veiledernavn) {
        return TOPP +
                "<p>Til " + navn.trim() + ", </p>" +
                "<p>NAV ønsker å avtale et dialogmøte med deg i forbindelse med at " +
                "en av dine ansatte er sykmeldt. Av hensyn til personvernet kan vi ikke oppgi navnet på den " +
                "sykmeldte i en e-post.</p>" +

                "<p>Klikk på denne <a href=\"" + url + "\">lenken</a> og oppgi hvilke tidspunkt som passer. " +
                "Vi ønsker svar så raskt som mulig og senest innen tre virkedager. </p>" +

                harDuSporsmal() +
                vennligHilsen(veiledernavn) +
                BUNN;
    }

    public static PEpost arbeidsgiverAvbrytMote(String navn, String veiledernavn, Mote Mote) {
        return new PEpost()
                .emne(arbeidsgiverAvbrytMoteEmne())
                .innhold(arbeidsgiverAvbrytMoteInnhold(navn, veiledernavn, Mote));
    }

    public static PEpost arbeidsgiverAvbrytBekreftetMote(String navn, String veiledernavn, Mote Mote) {
        return new PEpost()
                .emne(arbeidsgiverAvbrytMoteEmne())
                .innhold(arbeidsgiverAvbrytBekreftetMoteInnhold(navn, veiledernavn, Mote));
    }

    private static String arbeidsgiverAvbrytMoteEmne() {
        return "Dialogmøte med NAV avbrutt";
    }

    private static String arbeidsgiverAvbrytMoteInnhold(String navn, String veiledernavn, Mote Mote) {
        return TOPP +
                "<p>Til " + navn.trim() + ", </p>" +
                "<p>Du har tidligere mottatt en møteforespørsel på flere tidspunkter for et dialogmøte med en av dine sykmeldte. " +
                "Møteforespørselen er kansellert.</p>" +

                motetidspunker(Mote.alternativer) +
                motested(Mote.alternativer.get(0).sted) +

                harDuSporsmal() +
                vennligHilsen(veiledernavn) +
                BUNN;
    }

    private static String motetidspunker(List<TidOgSted> alternativer) {
        StringBuilder builder = new StringBuilder();

        if (alternativer.isEmpty()) {
            return "";
        } else if (alternativer.size() == 1) {
            builder.append("<p style=\"font-weight:bold\">Møtetidspunktet det gjelder:</p>");
        } else {
            builder.append("<p style=\"font-weight:bold\">Møteforespørsel og tidspunkter det gjelder:</p>");
        }

        alternativer = alternativer.stream().sorted((o1, o2) -> o2.tid.compareTo(o1.tid)).collect(toList());

        builder.append("<ol>");
        for (TidOgSted alternativ : alternativer) {
            builder.append("<li>" + tilLangDatoMedKlokkeslettPostfixDagPrefix(alternativ.tid) + "</li>");
        }
        builder.append("</ol>");

        return builder.toString();
    }

    private static String motested(String sted) {
        StringBuilder builder = new StringBuilder();
        builder.append("<p style=\"font-weight:bold\">Møtested</p>");
        builder.append("<p>" + sted + "</p>");
        return builder.toString();
    }

    private static String arbeidsgiverAvbrytBekreftetMoteInnhold(String navn, String veiledernavn, Mote Mote) {
        return TOPP +
                "<p>Til " + navn.trim() + ", </p>" +
                "<p>Du har tidligere mottatt en bekreftelse på et tidspunkt for et dialogmøte med en av dine sykmeldte. " +
                "Møteforespørselen er kansellert og du kan se bort fra henvendelsen.</p>" +

                motetidspunker(asList(Mote.valgtTidOgSted)) +
                motested(Mote.valgtTidOgSted.sted) +
                harDuSporsmal() +
                vennligHilsen(veiledernavn) +
                BUNN;
    }

    public static PEpost bekreftelseEpost(String navn, String uuid, String sted, LocalDateTime dato, String veiledernavn) {
        String epostInnhold = bekreftelsePEpost(navn, dato, veiledernavn);
        return new PEpost()
                .emne(bekreftelseEpostEmne())
                .innhold(epostInnhold)
                .vedlegg(asList(new PEpostVedlegg()
                        .innhold(bekreftelseEpostIcs(uuid, sted, dato))
                        .type("ICS")
                ));
    }

    private static String bekreftelseEpostEmne() {
        return "Bekreftelse på møte med NAV";
    }

    private static String bekreftelsePEpost(String navn, LocalDateTime dato, String veiledernavn) {
        return TOPP +
                "<p>Til " + navn.trim() + ", </p>" +
                "<p>Vi bekrefter møtetidspunkt " + tilLangDatoMedKlokkeslettPostfixDagPrefix(dato).toLowerCase()
                + ". Du vil om kort tid få en innkalling i posten med mer informasjon om dialogmøtet.</p>" +
                harDuSporsmal() +
                vennligHilsen(veiledernavn) +
                BUNN;
    }

    private static String bekreftelseEpostIcs(String uuid, String sted, LocalDateTime dato) {
        return icsInnhold(uuid, dato, bekreftelseEpostEmne(), sted, empty(), TENTATIVE);
    }

    public static PEpost arbeidsgiverNyeTidspunkt(String navn, String url, String veiledernavn) {
        return new PEpost()
                .emne(arbeidsgiverNyeTidspunktEmne())
                .innhold(arbeidsgiverNyeTidspunktInnhold(navn, url, veiledernavn));
    }

    private static String arbeidsgiverNyeTidspunktEmne() {
        return "Nye tidspunkt dialogmøte med NAV";
    }

    private static String arbeidsgiverNyeTidspunktInnhold(String navn, String url, String veiledernavn) {
        return TOPP +
                "<p>Til " + navn.trim() + ", </p>" +
                "<p>Vi har lagt til flere mulige tidspunkt i møteinnkallelsen du tidligere har mottatt.</p>" +

                "<p>Klikk på denne <a href=\"" + url + "\">lenken</a> og oppgi hvilke tidspunkt som passer. " +
                "Vi ønsker svar så raskt som mulig og senest innen tre virkedager. </p>" +

                harDuSporsmal() +
                vennligHilsen(veiledernavn) +
                BUNN;
    }

    public static final String TOPP = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional //EN\" " +
            "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"> " +
            "<meta http-equiv=\"Content-Type\" " +
            "content=\"text/html; " +
            "charset=UTF-8\" /> " +
            "<html>";

    public static final String BUNN = "<p>Du kan ikke svare på denne meldingen</p>" +
            "</html>";
}

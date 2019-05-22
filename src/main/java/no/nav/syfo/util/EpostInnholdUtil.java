package no.nav.syfo.util;

import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.TidOgSted;
import no.nav.syfo.repository.model.PEpost;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static no.nav.syfo.util.time.DateUtil.tilLangDatoMedKlokkeslettPostfixDagPrefix;

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
                "<p>Vi venter på svar fra deg om tidspunkt for dialogmøte.</p> " +

                "<p>Du kan svare på hvilke tidspunkt som passer ved å logge inn på " +
                "www.nav.no/dinesykmeldte eller følg denne lenken" + url + ". Da vil du også se hvem det gjelder.</p>" +

                "<p>Vennligst svar så raskt som mulig.</p> /p>" +

                harDuSporsmal() +
                vennligHilsen(veiledernavn) +
                BUNN;
    }

    private static String avlystMoteTidspunktOverskrift() {
        return "<p style=\"font-weight:bold\">Opprinnelig tidspunkt for møtet:</p>";
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

                "<p>Du kan svare på hvilke tidspunkt som passer ved å logge inn på " +
                "www.nav.no/dinesykmeldte eller følg denne lenken" + url + ". Da vil du også se hvem det gjelder.</p>" +

                "<p>Vi ønsker svar så raskt som mulig og senest innen tre virkedager.</p>" +

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
        return "Dialogmøte med NAV avlyst";
    }

    private static String arbeidsgiverAvbrytMoteInnhold(String navn, String veiledernavn, Mote Mote) {
        return TOPP +
                "<p>Til " + navn.trim() + ", </p>" +
                "<p>Du har tidligere fått en forespørsel om dialogmøte. Dette møtet vil likevel ikke bli avholdt. " +
                "Ta gjerne kontakt hvis du trenger å vite mer om grunnen.</p>" +

                avlystMoteTidspunktOverskrift() +

                motetidspunkt(Mote.alternativer) +

                harDuSporsmal() +
                vennligHilsen(veiledernavn) +
                BUNN;
    }

    public static String motetidspunkt(List<TidOgSted> alternativer) {
        StringBuilder builder = new StringBuilder();

        alternativer = alternativer.stream().sorted((o1, o2) -> o2.tid.compareTo(o1.tid)).collect(toList());

        builder.append("<ol>");
        for (TidOgSted alternativ : alternativer) {
            builder.append("<li>" + tilLangDatoMedKlokkeslettPostfixDagPrefix(alternativ.tid) + "</li>");
        }
        builder.append("</ol>");

        return builder.toString();
    }

    public static String motested(String sted) {
        StringBuilder builder = new StringBuilder();
        builder.append("<p style=\"font-weight:bold\">Møtested</p>");
        builder.append("<p>" + sted + "</p>");
        return builder.toString();
    }

    private static String arbeidsgiverAvbrytBekreftetMoteInnhold(String navn, String veiledernavn, Mote Mote) {
        return TOPP +
                "<p>Til " + navn.trim() + ", </p>" +
                "<p>Du har tidligere fått bekreftelse på tidspunktet for dialogmøte med en av dine sykmeldte. " +
                "Møtet vil likevel ikke bli avholdt, og du kan se bort fra henvendelsen. " +
                "Ta gjerne kontakt hvis du trenger å vite mer om grunnen.</p>" +

                avlystMoteTidspunktOverskrift() +
                motetidspunkt(asList(Mote.valgtTidOgSted)) +
                harDuSporsmal() +
                vennligHilsen(veiledernavn) +
                BUNN;
    }

    public static PEpost bekreftelseEpost(String navn, String url, String sted, LocalDateTime dato, String veiledernavn) {
        String epostInnhold = bekreftelsePEpost(navn, url, sted, dato, veiledernavn);
        return new PEpost()
                .emne(bekreftelseEpostEmne())
                .innhold(epostInnhold);
    }

    private static String bekreftelseEpostEmne() {
        return "Bekreftelse på møte med NAV";
    }

    private static String bekreftelsePEpost(String navn, String url, String sted, LocalDateTime dato, String veiledernavn) {
        return TOPP +
                "<p>Til " + navn.trim() + ", </p>" +

                "<p>Vi bekrefter tidspunkt for dialogmøte med NAV. "+
                "Av hensyn til personvernet kan vi ikke oppgi navnet til arbeidstakeren i en e-post. " +
                "Ønsker du å se hvem det gjelder, kan du logge inn på www.nav.no/dinesykmeldte eller følg denne lenken " +
                url +

                "<p style=\"font-weight:bold\">Møtetidspunkt</p>" +
                tilLangDatoMedKlokkeslettPostfixDagPrefix(dato).toLowerCase() +

                motested(sted) +

                "<p>Du vil om kort tid få en innkalling i posten med mer informasjon om dialogmøtet.</p>" +
                harDuSporsmal() +
                vennligHilsen(veiledernavn) +
                BUNN;
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
                "<p>Vi har foreslått nytt tidspunkt for dialogmøte.</p>" +

                "<p>Oppgi hvilke tidspunkt som passer ved å logge inn på " +
                "www.nav.no/dinesykmeldte eller følg denne lenken" + url + ". Da vil du også se hvem det gjelder.</p>" +

                "<p>Vi ønsker svar så raskt som mulig og senest innen tre virkedager.</p>" +

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

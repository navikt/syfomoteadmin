package no.nav.syfo.util;

import javax.activation.DataHandler;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static no.nav.syfo.config.converter.LocalDateTimeAdapter.marshalLocalDateTime;

public class OutlookEventUtil {

    public enum IcsStatus {
        TENTATIVE("PUBLISH", "TENTATIVE", 0),
        CONFIRMED("REQUEST", "CONFIRMED", 1),
        CANCELLED("CANCEL", "CANCELLED", 1);

        IcsStatus(String method, String status, Integer sequence) {
            this.method = method;
            this.status = status;
            this.sequence = sequence;
        }

        private String method;
        private String status;
        private Integer sequence;

    }

    public static Multipart lagOutlookForesporsel(String innhold) {
        Multipart multipart = new MimeMultipart();
        try {
            MimeBodyPart epostInnhold = new MimeBodyPart();
            epostInnhold.setContent(innhold, "text/html;charset=utf-8");
            multipart.addBodyPart(epostInnhold);
        } catch (Exception e) {
            throw new RuntimeException("Feil under opprettelsen av Outlook-Foresporsel", e);
        }
        return multipart;
    }

    public static Multipart addIcsFil(Multipart multipart, String innhold, String tittel) {
        try {
            multipart.addBodyPart(lagIcsFil(innhold, tittel));
        } catch (Exception e) {
            throw new RuntimeException("Feil under opprettelsen av Outlook-Foresporsel", e);
        }
        return multipart;
    }

    public static String icsInnhold(String uuid, LocalDateTime dato, String tittel, String motested, Optional<String> motelink, IcsStatus status) {
        try {
            return "BEGIN:VCALENDAR\n"
                    + "PRODID:-//Microsoft Corporation//Outlook9.0 MIMEDIR//EN\n"
                    + "VERSION:2.0\n"
                    + "X-WR-RELCALID:" + uuid + "\n"
                    + "METHOD:" + status.method + "\n"
                    + "BEGIN:VEVENT\n"
                    + "ATTENDEE;ROLE=REQ-PARTICIPANT;RSVP=FALSE\n"
                    + "ORGANIZER;CN=Nav;MAILTO:ikke-svar@nav.no\n"
                    + "STATUS:" + status.status + "\n"
                    + "DTSTART:" + marshalLocalDateTime(dato) + "\n"
                    + "DTEND:" + marshalLocalDateTime(dato.plusHours(1)) + "\n"
                    + "LOCATION:" + motested + "\n"
                    + "TRANSP:OPAQUE\n"
                    + "LAST-MODIFIED:" + marshalLocalDateTime(now()) + "\n"
                    //denne skal oppdatere seg pr. gang man gjør en endring på en epost. Pr nå er det kun opprett og avbryt/bekreft som skjer. Legger derfor ikke inn mer logikk.
                    + "SEQUENCE:" + status.sequence + "\n"
                    + "UID:" + uuid + "\n"
                    + "DTSTAMP:" + marshalLocalDateTime(dato) + "\n"
                    + "CATEGORIES:Meeting\n"
                    + "DESCRIPTION:" + tittel + " - " + motelink.orElse("") + "\n"
                    + "SUMMARY:" + tittel + "\n" + "PRIORITY:1\n"
                    + "CLASS:PUBLIC\n" + "BEGIN:VALARM\n"
                    + "TRIGGER:PT1440M\n" + "ACTION:DISPLAY\n"
                    + "DESCRIPTION:Reminder\n" + "END:VALARM\n"
                    + "END:VEVENT\n" + "END:VCALENDAR";
        } catch (Exception e) {
            throw new RuntimeException("Feil med marshallingen av datoene");
        }
    }

    private static MimeBodyPart lagIcsFil(String innhold, String tittel) throws Exception {
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(innhold, "text/iCalendar")));
        messageBodyPart.setFileName(tittel + ".ics");
        messageBodyPart.setHeader("Content-Class", "urn:content-classes:calendarmessage");
        messageBodyPart.setHeader("Content-ID", "calendar_message");
        return messageBodyPart;
    }
}

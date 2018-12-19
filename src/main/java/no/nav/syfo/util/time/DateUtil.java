package no.nav.syfo.util.time;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    public DateUtil() {
    }

    public static String tilKortDato(LocalDate dato) {
        return dato.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public static String tilKortDato(LocalDateTime dato) {
        return dato.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public static String tilKortDatoMedTid(LocalDateTime dato) {
        return dato.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public static LocalDateTime fraKortDatoMedTid(String dato) {
        return LocalDateTime.parse(dato, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public static String tilKortDatoMedKlokkeslettPostfix(LocalDateTime dato) {
        return dato.format(DateTimeFormatter.ofPattern("dd.MM")) + " kl. " + dato.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static String tilLangDatoMedKlokkeslettPostfix(LocalDateTime dato) {
        return dato.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " kl. " + dato.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static String tilLangDatoMedKlokkeslettPostfixDagPrefix(LocalDateTime dato) {
        return dag(dato.getDayOfWeek()) + " " + dato.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " kl. " + dato.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private static String dag(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return "Mandag";
            case TUESDAY:
                return "Tirsdag";
            case WEDNESDAY:
                return "Onsdag";
            case THURSDAY:
                return "Torsdag";
            case FRIDAY:
                return "Fredag";
            case SATURDAY:
                return "Lørdag";
            case SUNDAY:
                return "Søndag";
            default:
                return "";
        }
    }
}

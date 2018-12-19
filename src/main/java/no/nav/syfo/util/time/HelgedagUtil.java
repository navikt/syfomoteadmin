package no.nav.syfo.util.time;

import java.time.LocalDate;

public class HelgedagUtil {
    public HelgedagUtil() {
    }

    public static boolean erHelgedag(LocalDate dato) {
        return dato.getDayOfWeek().getValue() == 6 || dato.getDayOfWeek().getValue() == 7;
    }
}

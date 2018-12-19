package no.nav.syfo.util.time;

import java.time.LocalDate;

public class NorskeHelligDagerUtil {
    public NorskeHelligDagerUtil() {
    }

    public static boolean erNorskHelligDag(LocalDate dato) {
        int mnd = dato.getMonth().getValue();
        int dag = dato.getDayOfMonth();
        LocalDate paaskedag = hentFoerstePaaskedag(dato.getYear());
        if (foersteJanuaer(dag, mnd)) {
            return true;
        } else if (foesteMai(dag, mnd)) {
            return true;
        } else if (syttendeMai(dag, mnd)) {
            return true;
        } else if (foersteJuledag(dag, mnd)) {
            return true;
        } else if (andreJuledag(dag, mnd)) {
            return true;
        } else {
            return erPaaskeHelligdag(dato, paaskedag);
        }
    }

    private static boolean foersteJanuaer(int dag, int mnd) {
        return dag == 1 && mnd == 1;
    }

    private static boolean foesteMai(int dag, int mnd) {
        return dag == 1 && mnd == 5;
    }

    private static boolean syttendeMai(int dag, int mnd) {
        return dag == 17 && mnd == 5;
    }

    private static boolean foersteJuledag(int dag, int mnd) {
        return dag == 25 && mnd == 12;
    }

    private static boolean andreJuledag(int dag, int mnd) {
        return dag == 26 && mnd == 12;
    }

    private static boolean erPaaskeHelligdag(LocalDate dato, LocalDate paaskedag) {
        if (dato.isEqual(paaskedag.minusDays(7L))) {
            return true;
        } else if (dato.isEqual(paaskedag.minusDays(3L))) {
            return true;
        } else if (dato.isEqual(paaskedag.minusDays(2L))) {
            return true;
        } else if (dato.isEqual(paaskedag)) {
            return true;
        } else if (dato.isEqual(paaskedag.plusDays(1L))) {
            return true;
        } else if (dato.isEqual(paaskedag.plusDays(39L))) {
            return true;
        } else if (dato.isEqual(paaskedag.plusDays(49L))) {
            return true;
        } else {
            return dato.isEqual(paaskedag.plusDays(50L));
        }
    }

    private static LocalDate hentFoerstePaaskedag(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + (11 - h) + 22 * l) / 451;
        int n = (h + l - 7 * m + 114) / 31;
        int p = (h + l - 7 * m + 114) % 31;
        return LocalDate.of(year, n, p + 1);
    }
}

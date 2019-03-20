package no.nav.syfo.util;

import static java.lang.System.getenv;

public class RestUtils {

    public static String baseUrl() {
        return "https://app" + miljo() + ".adeo.no";
    }

    private static String miljo() {
        String environmentName = getenv("FASIT_ENVIRONMENT_NAME");
        if ("p".equals(environmentName)) {
            return "";
        }
        return "-" + environmentName;
    }
}

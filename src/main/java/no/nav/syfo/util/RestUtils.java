package no.nav.syfo.util;

import java.util.Base64;

import static java.lang.String.format;
import static java.lang.System.getProperty;

public class RestUtils {

    public static String baseUrl() {
        return "https://app" + miljo() + ".adeo.no";
    }

    private static String miljo() {
        if ("p".equals(getProperty("FASIT_ENVIRONMENT_NAME"))) {
            return "";
        }
        return "-" + getProperty("FASIT_ENVIRONMENT_NAME");
    }

    public static String basicCredentials(String credential) {
        return "Basic " + Base64.getEncoder().encodeToString(format("%s:%s", getProperty(credential + "_USERNAME"), getProperty(credential + "_PASSWORD")).getBytes());
    }
}

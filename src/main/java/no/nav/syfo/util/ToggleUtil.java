package no.nav.syfo.util;

import static java.lang.System.getProperty;

public final class ToggleUtil {

    private static final String DEFAULT_ON = "true";
    private static final String DEFAULT_OFF = "false";
    private static final String ENVIRONMENT_NAME = "FASIT_ENVIRONMENT_NAME";

    private static boolean getToggleDefaultOn(String key) {
        return !"false".equals(getProperty(key, DEFAULT_ON));
    }

    private static boolean getToggleDefaultOff(String key) {
        return "true".equals(getProperty(key, DEFAULT_OFF));
    }

    public static boolean toggleSendeEpost() {
        return getToggleDefaultOn("TOGGLES_SEND_EPOST");
    }

    public static boolean toggleDisablePolicies() {
        return getToggleDefaultOff("TOGGLES_DISABLE_POLICIES");
    }

    public static boolean toggleIgnorerEpostFeil() {
        return getToggleDefaultOff("TOGGLE_IGNOREREPOSTFEIL");
    }

    public static boolean kjorerIProduksjon() {
        return getProperty(ENVIRONMENT_NAME, "p").equalsIgnoreCase("p") ||
                getProperty(ENVIRONMENT_NAME).equalsIgnoreCase("q0");
    }

    public static boolean kjorerLokalt() {
        return getProperty(ENVIRONMENT_NAME, "p").equalsIgnoreCase("local");
    }
}

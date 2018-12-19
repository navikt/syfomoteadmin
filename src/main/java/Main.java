import no.nav.apiapp.ApiApp;
import no.nav.syfo.config.ApplicationConfig;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static java.lang.System.*;
import static no.nav.brukerdialog.security.Constants.OIDC_REDIRECT_URL_PROPERTY_NAME;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;
import static no.nav.syfo.config.ApplicationConfig.VEILARBLOGIN_REDIRECT_URL_URL;

public class Main {
    public static void main(String... args) throws Exception {
        getenv().forEach(System::setProperty);
        setupMetricsProperties();

        setProperty(OIDC_REDIRECT_URL_PROPERTY_NAME, getRequiredProperty(VEILARBLOGIN_REDIRECT_URL_URL));

        ApiApp.runApp(ApplicationConfig.class, args);
    }

    private static void setupMetricsProperties() throws UnknownHostException {
        setProperty("applicationName", ApplicationConfig.APPLICATION_NAME);
        setProperty("node.hostname", InetAddress.getLocalHost().getHostName());
        setProperty("environment.name", getProperty("FASIT_ENVIRONMENT_NAME"));
    }
}

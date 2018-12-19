import no.nav.apiapp.TestContext;
import no.nav.syfo.DatabaseTestContext;
import no.nav.testconfig.ApiAppTest;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static no.nav.sbl.dialogarena.test.SystemProperties.setFrom;
import static no.nav.testconfig.ApiAppTest.setupTestContext;
import static org.glassfish.jersey.server.ServerProperties.APPLICATION_NAME;

public class MainTest {
    private static final String PORT = "8801";

    public static void main(String[] args) throws Exception {
        setFrom("jetty-environment.properties");

        setupTestContext(ApiAppTest.Config
                .builder()
                .applicationName(APPLICATION_NAME)
                .build()
        );

        DatabaseTestContext.setupContext(getProperty("database"));
        TestContext.setup();

        setProperty("FASIT_ENVIRONMENT_NAME", "q1");

        Main.main(PORT);
    }
}

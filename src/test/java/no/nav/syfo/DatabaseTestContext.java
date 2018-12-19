package no.nav.syfo;

import lombok.val;
import no.nav.dialogarena.config.fasit.DbCredentials;
import no.nav.dialogarena.config.fasit.FasitUtils;
import no.nav.dialogarena.config.fasit.TestEnvironment;

import java.util.Optional;

import static no.nav.syfo.config.ApplicationConfig.APPLICATION_NAME;
import static no.nav.syfo.config.DatabaseConfig.MOTEADMINDB_PASSWORD;
import static no.nav.syfo.config.DatabaseConfig.MOTEADMINDB_URL;
import static no.nav.syfo.config.DatabaseConfig.MOTEADMINDB_USERNAME;

public class DatabaseTestContext {

    public static void setupContext(String miljo) {
        val dbCredential = Optional.ofNullable(miljo)
                .map(TestEnvironment::valueOf)
                .map(testEnvironment -> FasitUtils.getDbCredentials(testEnvironment, APPLICATION_NAME));

        if (dbCredential.isPresent()) {
            setDataSourceProperties(dbCredential.get());
        } else {
            setInMemoryDataSourceProperties();
        }

    }

    public static void setupInMemoryContext() {
        setupContext(null);
    }

    private static void setDataSourceProperties(DbCredentials dbCredentials) {
        System.setProperty(MOTEADMINDB_URL, dbCredentials.url);
        System.setProperty(MOTEADMINDB_USERNAME, dbCredentials.getUsername());
        System.setProperty(MOTEADMINDB_PASSWORD, dbCredentials.getPassword());

    }

    private static void setInMemoryDataSourceProperties() {
        System.setProperty(MOTEADMINDB_URL,
                "jdbc:h2:mem:moteadmin;DB_CLOSE_DELAY=-1;MODE=Oracle");
        System.setProperty(MOTEADMINDB_USERNAME, "sa");
        System.setProperty(MOTEADMINDB_PASSWORD, "password");
    }
}

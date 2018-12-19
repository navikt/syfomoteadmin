package no.nav.syfo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import no.nav.syfo.repository.dao.*;
import no.nav.sbl.jdbc.Database;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

import static java.lang.System.getProperty;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {
    public static final String MOTEADMINDB_URL = "MOTEADMINDB_URL";
    public static final String MOTEADMINDB_USERNAME = "MOTEADMINDB_USERNAME";
    public static final String MOTEADMINDB_PASSWORD = "MOTEADMINDB_PASSWORD";

    @Bean
    public static DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getProperty(MOTEADMINDB_URL));
        config.setUsername(getProperty(MOTEADMINDB_USERNAME));
        config.setPassword(getProperty(MOTEADMINDB_PASSWORD));
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        return new HikariDataSource(config);
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(dataSource());
    }

    @Bean
    public Database database(JdbcTemplate jdbcTemplate) {
        return new Database(jdbcTemplate);
    }

    @Bean
    public MoteDAO moteDAO() {
        return new MoteDAO();
    }

    @Bean
    public TidOgStedDAO tidOgStedDAO() {
        return new TidOgStedDAO();
    }

    @Bean
    public MotedeltakerDAO motedeltakerDAO() {
        return new MotedeltakerDAO();
    }

    @Bean
    public HendelseDAO hendelseDAO() {
        return new HendelseDAO();
    }

    @Bean
    public EpostDAO epostDAO() {
        return new EpostDAO();
    }

    @Bean
    public FeedDAO feedDAO() {
        return new FeedDAO();
    }
}

package no.nav.syfo.config;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.metrics.aspects.CountAspect;
import no.nav.metrics.aspects.TimerAspect;
import no.nav.syfo.batch.config.ScheduledTaskConfig;
import no.nav.syfo.batch.scheduler.SchedulingConfigurerImpl;
import no.nav.syfo.config.cache.CacheConfig;
import no.nav.syfo.config.consumer.LdapConfig;
import no.nav.syfo.api.system.AuthorizationFilter;
import no.nav.syfo.filter.CORSFilter;
import org.springframework.context.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import static java.util.EnumSet.allOf;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan("no.nav.syfo")
@Import({
        ConsumerConfig.class,
        DatabaseConfig.class,
        ServiceConfig.class,
        CacheConfig.class,
        MessageQueueConfig.class,
        LdapConfig.class,
        ScheduledTaskConfig.class,
        SchedulingConfigurerImpl.class
})
public class ApplicationConfig implements ApiApplication.NaisApiApplication {
    public static final String APPLICATION_NAME = "syfomoteadmin";
    public static final String VEILARBLOGIN_REDIRECT_URL_URL = "VEILARBLOGIN_REDIRECT_URL_URL";

    @Bean
    public TimerAspect timerAspect() {
        return new TimerAspect();
    }

    @Bean
    public CountAspect countAspect() {
        return new CountAspect();
    }

    @Inject
    private DataSource dataSource;

    @Transactional
    @Override
    public void startup(ServletContext servletContext) {
        servletContext.addFilter(AuthorizationFilter.class.getSimpleName(), new AuthorizationFilter())
                .addMappingForUrlPatterns(allOf(DispatcherType.class), false, "/api/system/*");
        servletContext.addFilter(CORSFilter.class.getSimpleName(), new CORSFilter())
                .addMappingForUrlPatterns(allOf(DispatcherType.class), false, "/api/*");
    }

    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {
        apiAppConfigurator
                .addPublicPath("/api/system/.*")
                .issoLogin()
                .azureADB2CLogin()
                .sts();
    }
}

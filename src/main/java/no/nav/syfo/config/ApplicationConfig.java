package no.nav.syfo.config;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.metrics.aspects.CountAspect;
import no.nav.metrics.aspects.TimerAspect;
import no.nav.syfo.config.cache.CacheConfig;
import no.nav.syfo.batch.config.ScheduledTaskConfig;
import no.nav.syfo.batch.scheduler.SchedulingConfigurerImpl;
import no.nav.syfo.config.consumer.LdapConfig;
import org.springframework.context.annotation.*;

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

    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {
        apiAppConfigurator
                .issoLogin()
                .azureADB2CLogin()
                .sts();
    }
}

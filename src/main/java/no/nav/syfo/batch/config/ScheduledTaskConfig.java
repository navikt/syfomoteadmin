package no.nav.syfo.batch.config;

import no.nav.syfo.batch.scheduler.EpostUtsendingScheduledTask;
import no.nav.syfo.batch.scheduler.PaaminnelseScheduledTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

@Configuration
public class ScheduledTaskConfig {

    @Bean(name = "paaminnelseScheduledTask")
    public PaaminnelseScheduledTask paaminnelseScheduledTask() {
        return new PaaminnelseScheduledTask();
    }

    @Bean(name = "epostUtsendingScheduledTask")
    public EpostUtsendingScheduledTask epostUtsendingScheduledTask() {
        return new EpostUtsendingScheduledTask();
    }

    @Bean
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler();
    }

}

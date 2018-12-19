package no.nav.syfo.batch.scheduler;

import no.nav.syfo.service.EpostService;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static java.lang.System.getProperty;

public class EpostUtsendingScheduledTask implements ScheduledTask {

    @Inject
    private EpostService epostService;

    @Transactional
    @Override
    public void run() {
        if (!"true".equals(getProperty("LOCAL_MOCK"))) {
            epostService.finnEpostForSending().forEach(epost -> {
                epostService.send(epost);
                epostService.slettEpostEtterSending(epost.id);
            });
        }
    }
}

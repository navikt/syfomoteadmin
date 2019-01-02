package no.nav.syfo.batch.scheduler;

import no.nav.syfo.service.EpostService;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static java.lang.System.getProperty;
import static no.nav.syfo.util.ToggleUtil.toggleBatch;
import static org.slf4j.LoggerFactory.getLogger;

public class EpostUtsendingScheduledTask implements ScheduledTask {

    private static final Logger LOG = getLogger(EpostUtsendingScheduledTask.class);

    @Inject
    private EpostService epostService;

    @Transactional
    @Override
    public void run() {
        if (!"true".equals(getProperty("LOCAL_MOCK")) && toggleBatch()) {
            LOG.info("TRACEBATCH: run {}", this.getClass().getName());

            epostService.finnEpostForSending().forEach(epost -> {
                epostService.send(epost);
                epostService.slettEpostEtterSending(epost.id);
            });
        } else {
            LOG.info("TRACEBATCH: Not run {}: Batch er togglet av", this.getClass().getName());
        }
    }
}

package no.nav.syfo.batch.scheduler;

import no.nav.syfo.repository.model.PEpost;
import no.nav.syfo.service.EpostService;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import java.util.List;
import java.util.Random;

import static java.lang.System.getProperty;
import static no.nav.syfo.util.ToggleUtil.toggleBatchEpost;
import static org.slf4j.LoggerFactory.getLogger;

public class EpostUtsendingScheduledTask implements ScheduledTask {

    private static final Logger LOG = getLogger(EpostUtsendingScheduledTask.class);

    @Inject
    private EpostService epostService;

    @Transactional
    @Override
    public void run() {
        if (!"true".equals(getProperty("LOCAL_MOCK")) && toggleBatchEpost()) {
            LOG.info("TRACEBATCH: run {}", this.getClass().getName());

            List<PEpost> epostListe = epostService.finnEpostForSending();

            Random rand = new Random();
            PEpost epost = epostListe.get(rand.nextInt(epostListe.size()));
            LOG.info("TRACEBATCH: run epost  {}", epost.id);
//            epostService.finnEpostForSending().forEach(epost -> {
                epostService.send(epost);
                epostService.slettEpostEtterSending(epost.id);
//            });
        }
    }
}

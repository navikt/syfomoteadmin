package no.nav.syfo.batch.scheduler;

import no.nav.syfo.service.MoteService;
import no.nav.syfo.service.MotedeltakerService;
import no.nav.syfo.service.varselinnhold.ArbeidsgiverVarselService;
import no.nav.syfo.util.DatoService;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDate;

import static no.nav.syfo.domain.model.Varseltype.PAAMINNELSE;
import static no.nav.syfo.util.ToggleUtil.toggleBatch;
import static no.nav.syfo.util.time.HelgedagUtil.erHelgedag;
import static no.nav.syfo.util.time.NorskeHelligDagerUtil.erNorskHelligDag;
import static org.slf4j.LoggerFactory.getLogger;

public class PaaminnelseScheduledTask implements ScheduledTask {

    private static final Logger LOG = getLogger(PaaminnelseScheduledTask.class);

    @Inject
    private MotedeltakerService motedeltakerService;
    @Inject
    private MoteService moteService;
    @Inject
    private ArbeidsgiverVarselService varselService;
    @Inject
    private DatoService datoService;

    @Transactional
    @Override
    public void run() {
        if (toggleBatch()) {
            LOG.info("TRACEBATCH: run {}", this.getClass().getName());

            int antallDagerBakoverEkstra = 0;
            LocalDate dato = datoService.dagensDato();

            if (erHelgedag(dato) || erNorskHelligDag(dato)) {
                LOG.info("Sender ikke påminnelser i dag");
                return;
            }
            dato = dato.minusDays(1);
            while (erHelgedag(dato) || erNorskHelligDag(dato)) {
                antallDagerBakoverEkstra++;
                dato = dato.minusDays(1);
            }

            LOG.info("Sender påminnelser");
            motedeltakerService.findMotedeltakereSomIkkeHarSvartSisteDognet(antallDagerBakoverEkstra)
                    .stream()
                    .map(motedeltaker -> moteService.findMoteByMotedeltakerUuid(motedeltaker.uuid))
                    .forEach(mote -> varselService.sendVarsel(PAAMINNELSE, mote));
        }
    }
}

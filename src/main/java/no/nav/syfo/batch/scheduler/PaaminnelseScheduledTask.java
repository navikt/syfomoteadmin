package no.nav.syfo.batch.scheduler;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.service.MoteService;
import no.nav.syfo.service.MotedeltakerService;
import no.nav.syfo.service.varselinnhold.ArbeidsgiverVarselService;
import no.nav.syfo.util.DatoService;
import no.nav.syfo.util.Toggle;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDate;

import static no.nav.syfo.domain.model.Varseltype.PAAMINNELSE;
import static no.nav.syfo.util.time.HelgedagUtil.erHelgedag;
import static no.nav.syfo.util.time.NorskeHelligDagerUtil.erNorskHelligDag;

@Slf4j
@Component
public class PaaminnelseScheduledTask {

    private final String SRV_BRUKER = "srvmoteadmin";

    private MotedeltakerService motedeltakerService;

    private MoteService moteService;

    private ArbeidsgiverVarselService varselService;

    private DatoService datoService;

    private Toggle toggle;

    @Inject
    public PaaminnelseScheduledTask(
            MotedeltakerService motedeltakerService,
            MoteService moteService,
            ArbeidsgiverVarselService varselService,
            DatoService datoService,
            Toggle toggle
    ) {
        this.motedeltakerService = motedeltakerService;
        this.moteService = moteService;
        this.varselService = varselService;
        this.datoService = datoService;
        this.toggle = toggle;
    }

    @Transactional
    @Scheduled(cron = "0 0 8 * * *")
    public void run() {
        if (toggle.toggleBatchPaaminelse()) {
            log.info("TRACEBATCH: run {}", this.getClass().getName());

            int antallDagerBakoverEkstra = 0;
            LocalDate dato = datoService.dagensDato();

            if (erHelgedag(dato) || erNorskHelligDag(dato)) {
                log.info("Sender ikke påminnelser i dag");
                return;
            }
            dato = dato.minusDays(1);
            while (erHelgedag(dato) || erNorskHelligDag(dato)) {
                antallDagerBakoverEkstra++;
                dato = dato.minusDays(1);
            }

            log.info("Sender påminnelser");
            motedeltakerService.findMotedeltakereSomIkkeHarSvartSisteDognet(antallDagerBakoverEkstra)
                    .stream()
                    .map(motedeltaker -> moteService.findMoteByMotedeltakerUuid(motedeltaker.uuid))
                    .forEach(mote -> varselService.sendVarsel(PAAMINNELSE, mote, true, SRV_BRUKER));
        }
    }
}

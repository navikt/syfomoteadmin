package no.nav.syfo.batch.scheduler;

import no.nav.syfo.batch.leaderelection.LeaderElectionService;
import no.nav.syfo.service.*;
import no.nav.syfo.service.varselinnhold.ArbeidsgiverVarselService;
import no.nav.syfo.util.*;
import org.slf4j.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDate;

import static no.nav.syfo.domain.model.Varseltype.PAAMINNELSE;
import static no.nav.syfo.util.time.HelgedagUtil.erHelgedag;
import static no.nav.syfo.util.time.NorskeHelligDagerUtil.erNorskHelligDag;

@Component
public class PaaminnelseScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(PaaminnelseScheduledTask.class);

    private final String SRV_BRUKER = "srvmoteadmin";

    private MotedeltakerService motedeltakerService;

    private MoteService moteService;

    private ArbeidsgiverVarselService varselService;

    private DatoService datoService;

    private Toggle toggle;

    private final LeaderElectionService leaderElectionService;

    @Inject
    public PaaminnelseScheduledTask(
            MotedeltakerService motedeltakerService,
            MoteService moteService,
            ArbeidsgiverVarselService varselService,
            DatoService datoService,
            Toggle toggle,
            LeaderElectionService leaderElectionService
    ) {
        this.motedeltakerService = motedeltakerService;
        this.moteService = moteService;
        this.varselService = varselService;
        this.datoService = datoService;
        this.toggle = toggle;
        this.leaderElectionService = leaderElectionService;
    }

    @Transactional
    @Scheduled(cron = "0 0 8 * * *")
    public void run() {
        if (toggle.toggleBatchPaaminelse() && leaderElectionService.isLeader()) {
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

package no.nav.syfo.service;

import no.nav.syfo.behandlendeenhet.BehandlendeEnhetConsumer;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.kafka.producer.OversikthendelseProducer;
import no.nav.syfo.kafka.producer.OversikthendelseType;
import no.nav.syfo.kafka.producer.model.KOversikthendelse;
import org.springframework.stereotype.Service;

import static java.time.LocalDateTime.now;

@Service
public class OversikthendelseService {

    private AktoerService aktoerService;
    private final BehandlendeEnhetConsumer behandlendeEnhetConsumer;
    private OversikthendelseProducer oversikthendelseProducer;

    public OversikthendelseService(
            AktoerService aktoerService,
            BehandlendeEnhetConsumer behandlendeEnhetConsumer,
            OversikthendelseProducer oversikthendelseProducer
    ) {
        this.aktoerService = aktoerService;
        this.behandlendeEnhetConsumer = behandlendeEnhetConsumer;
        this.oversikthendelseProducer = oversikthendelseProducer;
    }

    public void sendOversikthendelse(Mote mote, OversikthendelseType type) {
        String sykmeldtFnr = aktoerService.hentFnrForAktoer(mote.sykmeldt().aktorId);
        String behandlendeEnhet = behandlendeEnhetConsumer.getBehandlendeEnhet(sykmeldtFnr).getEnhetId();

        KOversikthendelse kOversikthendelse = KOversikthendelse.builder()
                .fnr(sykmeldtFnr)
                .hendelseId(type.name())
                .enhetId(behandlendeEnhet)
                .tidspunkt(now()
                ).build();

        oversikthendelseProducer.sendOversikthendelse(kOversikthendelse);
    }
}

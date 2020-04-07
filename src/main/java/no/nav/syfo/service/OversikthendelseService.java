package no.nav.syfo.service;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.aktorregister.domain.AktorId;
import no.nav.syfo.behandlendeenhet.BehandlendeEnhetConsumer;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.kafka.producer.OversikthendelseProducer;
import no.nav.syfo.kafka.producer.OversikthendelseType;
import no.nav.syfo.kafka.producer.model.KOversikthendelse;
import org.springframework.stereotype.Service;

import static java.time.LocalDateTime.now;

@Service
public class OversikthendelseService {

    private final AktorregisterConsumer aktorregisterConsumer;
    private final BehandlendeEnhetConsumer behandlendeEnhetConsumer;
    private OversikthendelseProducer oversikthendelseProducer;

    public OversikthendelseService(
            AktorregisterConsumer aktorregisterConsumer,
            BehandlendeEnhetConsumer behandlendeEnhetConsumer,
            OversikthendelseProducer oversikthendelseProducer
    ) {
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.behandlendeEnhetConsumer = behandlendeEnhetConsumer;
        this.oversikthendelseProducer = oversikthendelseProducer;
    }

    public void sendOversikthendelse(Mote mote, OversikthendelseType type) {
        String sykmeldtFnr = aktorregisterConsumer.getFnrForAktorId(new AktorId(mote.sykmeldt().aktorId));
        String behandlendeEnhet = behandlendeEnhetConsumer.getBehandlendeEnhet(sykmeldtFnr, null).getEnhetId();

        KOversikthendelse kOversikthendelse = KOversikthendelse.builder()
                .fnr(sykmeldtFnr)
                .hendelseId(type.name())
                .enhetId(behandlendeEnhet)
                .tidspunkt(now()
                ).build();

        oversikthendelseProducer.sendOversikthendelse(kOversikthendelse);
    }
}

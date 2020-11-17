package no.nav.syfo.service;

import no.nav.syfo.consumer.pdl.PdlConsumer;
import no.nav.syfo.domain.AktorId;
import no.nav.syfo.consumer.behandlendeenhet.BehandlendeEnhetConsumer;
import no.nav.syfo.domain.Fodselsnummer;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.kafka.producer.OversikthendelseProducer;
import no.nav.syfo.kafka.producer.OversikthendelseType;
import no.nav.syfo.kafka.producer.model.KOversikthendelse;
import org.springframework.stereotype.Service;

import static java.time.LocalDateTime.now;

@Service
public class OversikthendelseService {

    private final BehandlendeEnhetConsumer behandlendeEnhetConsumer;
    private final PdlConsumer pdlConsumer;
    private OversikthendelseProducer oversikthendelseProducer;

    public OversikthendelseService(
            BehandlendeEnhetConsumer behandlendeEnhetConsumer,
            PdlConsumer pdlConsumer,
            OversikthendelseProducer oversikthendelseProducer
    ) {
        this.behandlendeEnhetConsumer = behandlendeEnhetConsumer;
        this.pdlConsumer = pdlConsumer;
        this.oversikthendelseProducer = oversikthendelseProducer;
    }

    public void sendOversikthendelse(Mote mote, OversikthendelseType type) {
        Fodselsnummer sykmeldtFnr = pdlConsumer.fodselsnummer(new AktorId(mote.sykmeldt().aktorId));
        String behandlendeEnhet = behandlendeEnhetConsumer.getBehandlendeEnhet(sykmeldtFnr.getValue(), null).getEnhetId();

        KOversikthendelse kOversikthendelse = KOversikthendelse.builder()
                .fnr(sykmeldtFnr.getValue())
                .hendelseId(type.name())
                .enhetId(behandlendeEnhet)
                .tidspunkt(now()
                ).build();

        oversikthendelseProducer.sendOversikthendelse(mote.uuid, kOversikthendelse);
    }
}

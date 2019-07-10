package no.nav.syfo.service;

import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.kafka.producer.OversikthendelseProducer;
import no.nav.syfo.kafka.producer.OversikthendelseType;
import no.nav.syfo.kafka.producer.model.KOversikthendelse;
import org.springframework.stereotype.Service;

import static java.time.LocalDateTime.now;

@Service
public class OversikthendelseService {

    private AktoerService aktoerService;
    private EnhetService enhetService;
    private OversikthendelseProducer oversikthendelseProducer;

    public OversikthendelseService(
            AktoerService aktoerService,
            EnhetService enhetService,
            OversikthendelseProducer oversikthendelseProducer
    ) {
        this.aktoerService = aktoerService;
        this.enhetService = enhetService;
        this.oversikthendelseProducer = oversikthendelseProducer;
    }

    public void sendOversikthendelse(Mote mote, OversikthendelseType type) {
        String sykmeldtFnr = aktoerService.hentFnrForAktoer(mote.sykmeldt().aktorId);
        String behandlendeEnhet = enhetService.finnArbeidstakersBehandlendeEnhet(sykmeldtFnr);

        KOversikthendelse kOversikthendelse = KOversikthendelse.builder()
                .fnr(sykmeldtFnr)
                .hendelseId(type.name())
                .enhetId(behandlendeEnhet)
                .tidspunkt(now()
                ).build();

        oversikthendelseProducer.sendOversikthendelse(kOversikthendelse);
    }
}

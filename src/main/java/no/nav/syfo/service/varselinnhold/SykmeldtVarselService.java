package no.nav.syfo.service.varselinnhold;

import no.nav.syfo.consumer.dkif.DigitalKontaktinfo;
import no.nav.syfo.consumer.dkif.DkifConsumer;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.Varseltype;
import no.nav.syfo.service.ServiceVarselService;
import no.nav.syfo.service.mq.MqOppgaveVarselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static no.nav.syfo.domain.model.Varseltype.*;

@Service
public class SykmeldtVarselService {

    private DkifConsumer dkifConsumer;

    private MqOppgaveVarselService mqOppgaveVarselService;

    private ServiceVarselService serviceVarselService;

    @Autowired
    public SykmeldtVarselService(
            DkifConsumer dkifConsumer,
            MqOppgaveVarselService mqOppgaveVarselService,
            ServiceVarselService serviceVarselService
    ) {
        this.dkifConsumer = dkifConsumer;
        this.mqOppgaveVarselService = mqOppgaveVarselService;
        this.serviceVarselService = serviceVarselService;
    }

    public void sendVarsel(Varseltype varseltype, Mote mote) {
        DigitalKontaktinfo kontaktinfo = dkifConsumer.kontaktinformasjon(mote.sykmeldt().aktorId);
        if (!kontaktinfo.getKanVarsles()) {
            return;
        }
        if (varseltype == OPPRETTET) {
            mqOppgaveVarselService.sendOppgaveVarsel(mote.sykmeldt().aktorId, mote.sykmeldt().uuid);
        } else if (varseltype == AVBRUTT_BEKREFTET) {
            serviceVarselService.sendServiceVarsel(mote.sykmeldt().aktorId, mote.sykmeldt().uuid, "NaermesteLederMoteAvbrutt");
        } else if (varseltype == AVBRUTT) {
            serviceVarselService.sendServiceVarsel(mote.sykmeldt().aktorId, mote.sykmeldt().uuid, "SyfoMoteAvbrutt");
        } else if (varseltype == BEKREFTET) {
            serviceVarselService.sendServiceVarsel(mote.sykmeldt().aktorId, mote.sykmeldt().uuid, "SyfoMotebekreftelse", mote);
        } else if (varseltype == NYE_TIDSPUNKT) {
            serviceVarselService.sendServiceVarsel(mote.sykmeldt().aktorId, mote.sykmeldt().uuid, "SyfomoteNyetidspunkt");
        }
    }
}

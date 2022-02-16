package no.nav.syfo.service.varselinnhold;

import no.nav.syfo.consumer.dkif.DigitalKontaktinfo;
import no.nav.syfo.consumer.dkif.DkifConsumer;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.Varseltype;
import no.nav.syfo.service.ServiceVarselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static no.nav.syfo.domain.model.Varseltype.*;

@Service
public class SykmeldtVarselService {

    private DkifConsumer dkifConsumer;

    private ServiceVarselService serviceVarselService;

    @Autowired
    public SykmeldtVarselService(
            DkifConsumer dkifConsumer,
            ServiceVarselService serviceVarselService
    ) {
        this.dkifConsumer = dkifConsumer;
        this.serviceVarselService = serviceVarselService;
    }

    public void sendVarsel(Varseltype varseltype, Mote mote) {
        String aktorId = mote.sykmeldt().aktorId;
        DigitalKontaktinfo kontaktinfo = dkifConsumer.kontaktinformasjon(aktorId);
        if (!kontaktinfo.getKanVarsles()) {
            return;
        }
        String uuid = mote.sykmeldt().uuid;
        if (varseltype == OPPRETTET) {
            serviceVarselService.sendServiceVarsel(aktorId, uuid, "SyfoMoteForesporsel");
        } else if (varseltype == AVBRUTT_BEKREFTET) {
            serviceVarselService.sendServiceVarsel(aktorId, uuid, "NaermesteLederMoteAvbrutt");
        } else if (varseltype == AVBRUTT) {
            serviceVarselService.sendServiceVarsel(aktorId, uuid, "SyfoMoteAvbrutt");
        } else if (varseltype == BEKREFTET) {
            serviceVarselService.sendServiceVarsel(aktorId, uuid, "SyfoMotebekreftelse", mote);
        } else if (varseltype == NYE_TIDSPUNKT) {
            serviceVarselService.sendServiceVarsel(aktorId, uuid, "SyfomoteNyetidspunkt");
        }
    }
}

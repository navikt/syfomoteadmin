package no.nav.syfo.service.varselinnhold;

import no.nav.syfo.domain.model.Kontaktinfo;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.Varseltype;
import no.nav.syfo.oidc.OIDCIssuer;
import no.nav.syfo.service.DkifService;
import no.nav.syfo.service.ServiceVarselService;
import no.nav.syfo.service.mq.MqOppgaveVarselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static no.nav.syfo.domain.model.Varseltype.*;

@Service
public class SykmeldtVarselService {

    private DkifService dkifService;

    private MqOppgaveVarselService mqOppgaveVarselService;

    private ServiceVarselService serviceVarselService;

    @Autowired
    public SykmeldtVarselService(
            DkifService dkifService,
            MqOppgaveVarselService mqOppgaveVarselService,
            ServiceVarselService serviceVarselService
    ) {
        this.dkifService = dkifService;
        this.mqOppgaveVarselService = mqOppgaveVarselService;
        this.serviceVarselService = serviceVarselService;
    }

    public void sendVarsel(Varseltype varseltype, Mote mote) {
        Kontaktinfo kontaktinfo = dkifService.hentKontaktinfoAktoerId(mote.sykmeldt().aktorId, OIDCIssuer.INTERN);
        if (!kontaktinfo.skalHaVarsel) {
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

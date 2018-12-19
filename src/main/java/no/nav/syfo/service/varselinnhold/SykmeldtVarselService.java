package no.nav.syfo.service.varselinnhold;

import no.nav.syfo.domain.model.Kontaktinfo;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.Varseltype;
import no.nav.syfo.service.DkifService;
import no.nav.syfo.service.mq.MqOppgaveVarselService;
import no.nav.syfo.service.ServiceVarselService;

import javax.inject.Inject;

import static no.nav.syfo.domain.model.Varseltype.*;

public class SykmeldtVarselService {
    @Inject
    private DkifService dkifService;
    @Inject
    private MqOppgaveVarselService mqOppgaveVarselService;
    @Inject
    private ServiceVarselService serviceVarselService;

    public void sendVarsel(Varseltype varseltype, Mote mote) {
        Kontaktinfo kontaktinfo = dkifService.hentKontaktinfoAktoerId(mote.sykmeldt().aktorId);
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
            serviceVarselService.sendServiceVarsel(mote.sykmeldt().aktorId, mote.sykmeldt().uuid, "SyfoMotebekreftelse");
        } else if (varseltype == NYE_TIDSPUNKT) {
            serviceVarselService.sendServiceVarsel(mote.sykmeldt().aktorId, mote.sykmeldt().uuid, "SyfomoteNyetidspunkt");
        }
    }
}

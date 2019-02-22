package no.nav.syfo.service.varselinnhold;

import no.nav.syfo.domain.model.TidOgSted;
import no.nav.syfo.domain.model.Varseltype;
import no.nav.syfo.domain.model.Veileder;
import no.nav.syfo.repository.model.PEpost;
import no.nav.syfo.service.EpostService;
import no.nav.syfo.service.HendelseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.nav.syfo.domain.model.Varseltype.*;
import static no.nav.syfo.util.EpostInnholdVeilederUtil.*;

@Service
public class VeilederVarselService {

    private EpostService epostService;

    private HendelseService hendelseService;

    @Autowired
    public VeilederVarselService(
            EpostService epostService,
            HendelseService hendelseService
    ) {
        this.epostService = epostService;
        this.hendelseService = hendelseService;
    }

    public void sendVarsel(Varseltype varseltype, Veileder veileder) {
        if (varseltype == OPPRETTET) {
            epostService.klargjorForSending(opprettetEpostVeileder(veileder, veileder.mote.alternativer).mottaker(veileder.epost));
        } else if (varseltype.equals(BEKREFTET)) {
            epostService.klargjorForSending(avvistMoteTidspunkt(veileder).mottaker(veileder.epost));
            epostService.klargjorForSending(bekreftetEpostVeileder(veileder, veileder.mote.valgtTidOgSted).mottaker(veileder.epost));
        } else if (varseltype.equals(AVBRUTT)) {
            epostService.klargjorForSending(avvistMoteTidspunkt(veileder).mottaker(veileder.epost));
        } else if (varseltype.equals(NYE_TIDSPUNKT)) {
            epostService.klargjorForSending(flereTidspunktEpostVeileder(veileder, veileder.mote.alternativer).mottaker(veileder.epost));
        } else {
            return;
        }
        hendelseService.opprettHendelseVarselVeileder(varseltype, veileder);
    }

    private PEpost avvistMoteTidspunkt(Veileder veileder) {
        List<TidOgSted> avvisteAlternativer = veileder.mote.alternativer.stream()
                .filter(alternativ -> veileder.mote.valgtTidOgSted == null || !alternativ.id.equals(veileder.mote.valgtTidOgSted.id))
                .collect(toList());
        return avvistEpostVeileder(veileder, avvisteAlternativer);
    }
}

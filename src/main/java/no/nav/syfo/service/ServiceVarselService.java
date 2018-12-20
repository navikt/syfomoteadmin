package no.nav.syfo.service;

import no.nav.melding.virksomhet.varsel.v1.varsel.XMLAktoerId;
import no.nav.melding.virksomhet.varsel.v1.varsel.XMLParameter;
import no.nav.melding.virksomhet.varsel.v1.varsel.XMLVarsel;
import no.nav.melding.virksomhet.varsel.v1.varsel.XMLVarslingstyper;
import no.nav.syfo.domain.model.Mote;
import org.springframework.jms.core.JmsTemplate;

import javax.inject.Inject;
import javax.inject.Named;

import static java.lang.System.getProperty;
import static no.nav.syfo.util.time.DateUtil.tilLangDatoMedKlokkeslettPostfixDagPrefix;
import static no.nav.syfo.util.JAXB.marshallVarsel;
import static no.nav.syfo.util.JmsUtil.messageCreator;

public class ServiceVarselService {
    @Inject
    @Named("servicevarselqueue")
    private JmsTemplate servicevarselqueue;
    @Inject
    private MoteService moteService;

    public void sendServiceVarsel(String aktoerId, String uuid, String varseltype) {
        XMLVarsel xmlVarsel = new XMLVarsel()
                .withMottaker(new XMLAktoerId(aktoerId))
                .withVarslingstype(new XMLVarslingstyper(varseltype, null, null))
                .withParameterListes(new XMLParameter("url", getProperty("TJENESTER_URL") + "/beskjed/melding/ny"));

        if ("SyfoMotebekreftelse".equals(varseltype)) {
            Mote Mote = moteService.findMoteByMotedeltakerUuid(uuid);
            xmlVarsel.getParameterListes().add(new XMLParameter("tidsted", tilLangDatoMedKlokkeslettPostfixDagPrefix(Mote.valgtTidOgSted.tid)));
        }

        String melding = marshallVarsel(xmlVarsel);
        servicevarselqueue.send(messageCreator(melding, uuid));
    }
}

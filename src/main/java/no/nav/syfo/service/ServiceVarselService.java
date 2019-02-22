package no.nav.syfo.service;

import no.nav.melding.virksomhet.varsel.v1.varsel.XMLAktoerId;
import no.nav.melding.virksomhet.varsel.v1.varsel.XMLParameter;
import no.nav.melding.virksomhet.varsel.v1.varsel.XMLVarsel;
import no.nav.melding.virksomhet.varsel.v1.varsel.XMLVarslingstyper;
import no.nav.syfo.domain.model.Mote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import static no.nav.syfo.util.JAXB.marshallVarsel;
import static no.nav.syfo.util.JmsUtil.messageCreator;
import static no.nav.syfo.util.time.DateUtil.tilLangDatoMedKlokkeslettPostfixDagPrefix;

@Service
public class ServiceVarselService {

    @Value("${tjenester.url}")
    private String tjenesterUrl;

    private JmsTemplate servicevarselqueue;

    @Autowired
    public ServiceVarselService(@Qualifier("servicevarselqueue") JmsTemplate servicevarselqueue) {
        this.servicevarselqueue = servicevarselqueue;
    }

    public void sendServiceVarsel(String aktoerId, String uuid, String varseltype) {
        XMLVarsel xmlVarsel = lagVarselXml(aktoerId, varseltype);

        send(xmlVarsel, uuid);
    }

    public void sendServiceVarsel(String aktoerId, String uuid, String varseltype, Mote moteBekreftet) {
        XMLVarsel xmlVarsel = lagVarselXml(aktoerId, varseltype);

        xmlVarsel.getParameterListes().add(new XMLParameter("tidsted", tilLangDatoMedKlokkeslettPostfixDagPrefix(moteBekreftet.valgtTidOgSted.tid)));

        send(xmlVarsel, uuid);
    }

    private void send(XMLVarsel xmlVarsel, String uuid) {
        String melding = marshallVarsel(xmlVarsel);
        servicevarselqueue.send(messageCreator(melding, uuid));
    }

    private XMLVarsel lagVarselXml(String aktoerId, String varseltype) {
        return new XMLVarsel()
                .withMottaker(new XMLAktoerId(aktoerId))
                .withVarslingstype(new XMLVarslingstyper(varseltype, null, null))
                .withParameterListes(new XMLParameter("url", tjenesterUrl + "/beskjed/melding/ny"));
    }
}

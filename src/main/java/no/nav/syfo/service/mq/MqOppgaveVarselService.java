package no.nav.syfo.service.mq;

import no.nav.melding.virksomhet.varselmedhandling.v1.varselmedhandling.AktoerId;
import no.nav.melding.virksomhet.varselmedhandling.v1.varselmedhandling.ObjectFactory;
import no.nav.melding.virksomhet.varselmedhandling.v1.varselmedhandling.Parameter;
import no.nav.melding.virksomhet.varselmedhandling.v1.varselmedhandling.VarselMedHandling;
import org.springframework.jms.core.JmsTemplate;

import javax.inject.Inject;
import javax.inject.Named;

import static java.lang.System.getProperty;
import static no.nav.syfo.util.JAXB.marshallOppgaveVarsel;
import static no.nav.syfo.util.JmsUtil.messageCreator;

public class MqOppgaveVarselService {
    @Inject
    @Named("opprettvarselqueue")
    public JmsTemplate opprettVarselQueue;

    @Inject
    private MqHenvendelseService mqHenvendelseService;

    public void sendOppgaveVarsel(String aktoerId, String uuid) {
        VarselMedHandling varselMedHandling = new VarselMedHandling();
        varselMedHandling.setVarselbestillingId(uuid);
        AktoerId aktoer = new AktoerId();
        aktoer.setAktoerId(aktoerId);
        varselMedHandling.setMottaker(aktoer);
        varselMedHandling.setReVarsel(false);
        varselMedHandling.setVarseltypeId("SyfoMoteforesporsel");

        Parameter url = new Parameter();
        url.setKey("url");
        url.setValue(makeVarselUrl(uuid));
        varselMedHandling.getParameterListe().add(url);

        mqHenvendelseService.opprettOppgaveIHenvendelse(aktoerId, uuid);
        String melding = marshallOppgaveVarsel(new ObjectFactory().createVarselMedHandling(varselMedHandling));
        opprettVarselQueue.send(messageCreator(melding, uuid));
    }

    private String makeVarselUrl(String varselbestillingsId) {
        return getProperty("TJENESTER_URL") + "/innloggingsinfo/type/oppgave/undertype/0002/varselid/" + varselbestillingsId;
    }
}

package no.nav.syfo.service.mq;

import no.nav.melding.virksomhet.varselmedhandling.v1.varselmedhandling.AktoerId;
import no.nav.melding.virksomhet.varselmedhandling.v1.varselmedhandling.ObjectFactory;
import no.nav.melding.virksomhet.varselmedhandling.v1.varselmedhandling.Parameter;
import no.nav.melding.virksomhet.varselmedhandling.v1.varselmedhandling.VarselMedHandling;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static no.nav.syfo.util.JAXB.marshallOppgaveVarsel;
import static no.nav.syfo.util.JmsUtil.messageCreator;

@Service
@Transactional
public class MqOppgaveVarselService {

    @Value("${tjenester.url}")
    private String tjenesterUrl;

    private JmsTemplate opprettVarselQueue;

    private MqHenvendelseService mqHenvendelseService;

    @Inject
    public MqOppgaveVarselService(
            @Qualifier("opprettVarselQueue") JmsTemplate opprettVarselQueue,
            MqHenvendelseService mqHenvendelseService
    ) {
        this.opprettVarselQueue = opprettVarselQueue;
        this.mqHenvendelseService = mqHenvendelseService;
    }

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
        return tjenesterUrl + "/innloggingsinfo/type/oppgave/undertype/0002/varselid/" + varselbestillingsId;
    }
}

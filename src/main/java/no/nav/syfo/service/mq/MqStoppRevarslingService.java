package no.nav.syfo.service.mq;

import no.nav.melding.virksomhet.stopprevarsel.v1.stopprevarsel.ObjectFactory;
import no.nav.melding.virksomhet.stopprevarsel.v1.stopprevarsel.StoppReVarsel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static no.nav.syfo.util.JAXB.marshallOppgaveVarsel;
import static no.nav.syfo.util.JmsUtil.messageCreator;

@Service
@Transactional
public class MqStoppRevarslingService {

    private JmsTemplate stoppvarselqueue;

    @Autowired
    public MqStoppRevarslingService(
            @Qualifier("stoppvarselqueue") JmsTemplate stoppvarselqueue
    ) {
        this.stoppvarselqueue = stoppvarselqueue;
    }

    public void stoppReVarsel(String motedeltakerUuid) {
        StoppReVarsel stoppReVarsel = new StoppReVarsel();
        stoppReVarsel.setVarselbestillingId(motedeltakerUuid);
        String melding = marshallOppgaveVarsel(new ObjectFactory().createStoppReVarsel(stoppReVarsel));
        stoppvarselqueue.send(messageCreator(melding, motedeltakerUuid));
    }
}

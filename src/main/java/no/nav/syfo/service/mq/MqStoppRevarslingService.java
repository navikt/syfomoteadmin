package no.nav.syfo.service.mq;

import no.nav.melding.virksomhet.stopprevarsel.v1.stopprevarsel.ObjectFactory;
import no.nav.melding.virksomhet.stopprevarsel.v1.stopprevarsel.StoppReVarsel;
import org.springframework.jms.core.JmsTemplate;

import javax.inject.Inject;
import javax.inject.Named;

import static no.nav.syfo.util.JAXB.marshallOppgaveVarsel;
import static no.nav.syfo.util.JmsUtil.messageCreator;

public class MqStoppRevarslingService {
    @Inject
    @Named("stoppvarselqueue")
    private JmsTemplate stoppvarselqueue;

    public void stoppReVarsel(String motedeltakerUuid) {
        StoppReVarsel stoppReVarsel = new StoppReVarsel();
        stoppReVarsel.setVarselbestillingId(motedeltakerUuid);
        String melding = marshallOppgaveVarsel(new ObjectFactory().createStoppReVarsel(stoppReVarsel));
        stoppvarselqueue.send(messageCreator(melding, motedeltakerUuid));
    }
}

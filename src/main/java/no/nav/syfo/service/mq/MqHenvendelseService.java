package no.nav.syfo.service.mq;

import no.nav.melding.virksomhet.opprettoppgavehenvendelse.v1.opprettoppgavehenvendelse.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import static no.nav.syfo.util.JAXB.marshallHenvendelseOppgave;
import static no.nav.syfo.util.JmsUtil.messageCreator;

@Service
public class MqHenvendelseService {

    @Value("${tjenester.url}")
    private String tjenesterUrl;

    private JmsTemplate oppgavehenvendelsequeue;

    @Autowired
    public MqHenvendelseService(
            @Qualifier("oppgavehenvendelsequeue") JmsTemplate oppgavehenvendelsequeue
    ) {
        this.oppgavehenvendelsequeue = oppgavehenvendelsequeue;
    }

    public void opprettOppgaveIHenvendelse(String aktorId, String varselbestillingId) {
        Oppgavehenvendelse oppgavehenvendelse = new Oppgavehenvendelse();
        oppgavehenvendelse.setOppgaveURL(createOppgaveLenke());
        oppgavehenvendelse.setStoppRepeterendeVarsel(false);
        OppgaveType oppgaveType = new OppgaveType();
        oppgaveType.setValue("0002");
        oppgavehenvendelse.setOppgaveType(oppgaveType);
        oppgavehenvendelse.setVarselbestillingId(varselbestillingId);
        AktoerId aktoerId = new AktoerId();
        aktoerId.setAktoerId(aktorId);
        oppgavehenvendelse.setMottaker(aktoerId);
        oppgavehenvendelsequeue.send(messageCreator(marshallHenvendelseOppgave(new ObjectFactory().createOppgavehenvendelse(oppgavehenvendelse)), varselbestillingId));
    }

    private String createOppgaveLenke() {
        return tjenesterUrl + "/sykefravaer/";
    }
}

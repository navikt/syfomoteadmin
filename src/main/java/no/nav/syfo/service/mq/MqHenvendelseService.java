package no.nav.syfo.service.mq;

import no.nav.melding.virksomhet.opprettoppgavehenvendelse.v1.opprettoppgavehenvendelse.AktoerId;
import no.nav.melding.virksomhet.opprettoppgavehenvendelse.v1.opprettoppgavehenvendelse.ObjectFactory;
import no.nav.melding.virksomhet.opprettoppgavehenvendelse.v1.opprettoppgavehenvendelse.OppgaveType;
import no.nav.melding.virksomhet.opprettoppgavehenvendelse.v1.opprettoppgavehenvendelse.Oppgavehenvendelse;
import org.springframework.jms.core.JmsTemplate;

import javax.inject.Inject;
import javax.inject.Named;

import static java.lang.System.getProperty;
import static no.nav.syfo.util.JAXB.marshallHenvendelseOppgave;
import static no.nav.syfo.util.JmsUtil.messageCreator;

public class MqHenvendelseService {

    @Inject
    @Named("oppgavehenvendelsequeue")
    private JmsTemplate koTilHenvendelse;

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
        koTilHenvendelse.send(messageCreator(marshallHenvendelseOppgave(new ObjectFactory().createOppgavehenvendelse(oppgavehenvendelse)), varselbestillingId));
    }

    private String createOppgaveLenke() {
        return getProperty("TJENESTER_URL") + "/sykefravaer/";
    }
}

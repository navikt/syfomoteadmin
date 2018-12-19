package no.nav.syfo.config.mqconfigs;

import com.ibm.mq.jms.MQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;

import static java.lang.System.getProperty;

@Configuration
public class HenvendelseMQConfig {

    private static final String HENVENDELSE_OPPGAVE_HENVENDELSE_QUEUENAME = "HENVENDELSE_OPPGAVE_HENVENDELSE_QUEUENAME";

    @Inject
    @Named("connectionFactory")
    private ConnectionFactory connectionFactory;

    @Bean
    public Destination opprettOppgaveHenvendelseDestination() throws JMSException {
        return new MQQueue(getProperty(HENVENDELSE_OPPGAVE_HENVENDELSE_QUEUENAME));
    }

    @Bean(name = "oppgavehenvendelsequeue")
    public JmsTemplate oppgaveHenvendelseQueue() throws JMSException {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestination(opprettOppgaveHenvendelseDestination());
        jmsTemplate.setConnectionFactory(connectionFactory);
        return jmsTemplate;
    }

}

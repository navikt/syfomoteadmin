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
public class VarselMQConfig {

    private static final String VARSELPRODUKSJON_BEST_VARSEL_M_HANDLING_QUEUENAME = "VARSELPRODUKSJON_BEST_VARSEL_M_HANDLING_QUEUENAME";
    private static final String VARSELPRODUKSJON_STOPP_VARSEL_UTSENDING_QUEUENAME = "VARSELPRODUKSJON_STOPP_VARSEL_UTSENDING_QUEUENAME";
    private static final String VARSELPRODUKSJON_VARSLINGER_QUEUENAME = "VARSELPRODUKSJON_VARSLINGER_QUEUENAME";

    @Inject
    @Named("connectionFactory")
    private ConnectionFactory connectionFactory;

    @Bean
    @Named("oppgaveVarselDestination")
    public Destination oppgaveVarselDestination() throws JMSException {
        return new MQQueue(getProperty(VARSELPRODUKSJON_BEST_VARSEL_M_HANDLING_QUEUENAME));
    }

    @Bean
    @Named("stoppVarselDestination")
    public Destination stoppVarselDestination() throws JMSException {
        return new MQQueue(getProperty(VARSELPRODUKSJON_STOPP_VARSEL_UTSENDING_QUEUENAME));
    }

    @Bean
    @Named("serviceVarselDestination")
    public Destination serviceVarselDestination() throws JMSException {
        return new MQQueue(getProperty(VARSELPRODUKSJON_VARSLINGER_QUEUENAME));
    }

    @Bean(name = "opprettvarselqueue")
    public JmsTemplate opprettvarselqueue() throws JMSException {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestination(oppgaveVarselDestination());
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }

    @Bean(name = "stoppvarselqueue")
    public JmsTemplate stoppvarselqueue() throws JMSException {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestination(stoppVarselDestination());
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }

    @Bean(name = "servicevarselqueue")
    public JmsTemplate servicevarselqueue() throws JMSException {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestination(serviceVarselDestination());
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }
}

package no.nav.syfo.config;

import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQXAConnectionFactory;
import no.nav.syfo.config.mqconfigs.mq.UserCredentialsXaConnectionFactoryAdapter;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.jms.XAConnectionFactoryWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;

import javax.jms.*;

import static com.ibm.mq.constants.CMQC.MQENC_NATIVE;
import static com.ibm.msg.client.jms.JmsConstants.JMS_IBM_CHARACTER_SET;
import static com.ibm.msg.client.jms.JmsConstants.JMS_IBM_ENCODING;
import static com.ibm.msg.client.wmq.common.CommonConstants.WMQ_CM_CLIENT;

@Configuration
@EnableJms
@Profile({"remote"})
public class MessageQueueConfig {
    private static final int UTF_8_WITH_PUA = 1208;

    @Value("${syfomoteadmin.channel.name}")
    private String channelName;
    @Value("${mqgateway03.hostname}")
    private String gatewayHostname;
    @Value("${mqgateway03.name}")
    private String gatewayName;
    @Value("${mqgateway03.port}")
    private int gatewayPort;
    @Value("${srvappserver.username:srvappserver}")
    private String srvAppserverUsername;
    @Value("${srvappserver.password:}")
    private String srvAppserverPassword;

    @Bean(name = "opprettOppgaveHenvendelseDestination")
    public Queue opprettOppgaveHenvendelseDestination(@Value("${henvendelseoppgavevarsel.queuename}") String henvendelseoppgaveQueueName) throws JMSException {
        return new MQQueue(henvendelseoppgaveQueueName);
    }

    @Bean(name = "oppgavehenvendelsequeue")
    public JmsTemplate oppgaveHenvendelseQueue(
            @Autowired @Qualifier("opprettOppgaveHenvendelseDestination") Queue opprettOppgaveHenvendelseDestination,
            ConnectionFactory connectionFactory
    ) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestination(opprettOppgaveHenvendelseDestination);
        jmsTemplate.setConnectionFactory(connectionFactory);
        return jmsTemplate;
    }

    @Bean(name = "oppgaveVarselDestination")
    public Queue oppgaveVarselDestination(@Value("${bestvarselmhandling.queuename}") String bestvarselmhandlingQueueName) throws JMSException {
        return new MQQueue(bestvarselmhandlingQueueName);
    }

    @Bean(name = "stoppVarselDestination")
    public Queue stoppVarselDestination(@Value("${stopprevarsel.queuename}") String stopprevarselQueueName) throws JMSException {
        return new MQQueue(stopprevarselQueueName);
    }

    @Bean(name = "serviceVarselDestination")
    public Queue serviceVarselDestination(@Value("${servicevarsel.queuename}") String servicevarselQueueName) throws JMSException {
        return new MQQueue(servicevarselQueueName);
    }

    @Bean(name = "tredjepartsvarselVarselDestination")
    public Queue tredjepartsVarselDestination(@Value("${tredjepartsvarsel.queuename}") String tredjepartsvarselQueueName) throws JMSException {
        return new MQQueue(tredjepartsvarselQueueName);
    }

    @Bean(name = "opprettVarselQueue")
    public JmsTemplate opprettVarselQueue(
            @Autowired @Qualifier("oppgaveVarselDestination") Queue oppgaveVarselDestination,
            ConnectionFactory connectionFactory
    ) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestination(oppgaveVarselDestination);
        jmsTemplate.setConnectionFactory(connectionFactory);
        return jmsTemplate;
    }

    @Bean(name = "stoppvarselqueue")
    public JmsTemplate stoppvarselqueue(
            @Autowired @Qualifier("stoppVarselDestination") Queue stoppVarselDestination,
            ConnectionFactory connectionFactory
    ) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestination(stoppVarselDestination);
        jmsTemplate.setConnectionFactory(connectionFactory);
        return jmsTemplate;
    }

    @Bean(name = "servicevarselqueue")
    public JmsTemplate servicevarselqueue(
            @Autowired @Qualifier("serviceVarselDestination") Queue serviceVarselDestination,
            ConnectionFactory connectionFactory
    ) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestination(serviceVarselDestination);
        jmsTemplate.setConnectionFactory(connectionFactory);
        return jmsTemplate;
    }

    @Bean(name = "tredjepartsvarselqueue")
    public JmsTemplate tredjepartsvarselqueue(
            @Autowired @Qualifier("tredjepartsvarselVarselDestination") Queue tredjepartsvarselVarselDestination,
            ConnectionFactory connectionFactory
    ) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestination(tredjepartsvarselVarselDestination);
        jmsTemplate.setConnectionFactory(connectionFactory);
        return jmsTemplate;
    }

    @Bean
    public DestinationResolver destinationResolver(ApplicationContext context) {
        return (session, destinationName, pubSubDomain) -> context.getBean(destinationName, Queue.class);
    }

    @Bean
    public ConnectionFactory connectionFactory(XAConnectionFactoryWrapper xaConnectionFactoryWrapper) throws Exception {
        MQXAConnectionFactory connectionFactory = new MQXAConnectionFactory();
        connectionFactory.setHostName(gatewayHostname);
        connectionFactory.setPort(gatewayPort);
        connectionFactory.setChannel(channelName);
        connectionFactory.setQueueManager(gatewayName);
        connectionFactory.setTransportType(WMQ_CM_CLIENT);
        connectionFactory.setCCSID(UTF_8_WITH_PUA);
        connectionFactory.setIntProperty(JMS_IBM_ENCODING, MQENC_NATIVE);
        connectionFactory.setIntProperty(JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA);
        UserCredentialsXaConnectionFactoryAdapter adapter = new UserCredentialsXaConnectionFactoryAdapter();
        adapter.setTargetConnectionFactory(connectionFactory);
        adapter.setUsername(srvAppserverUsername);
        adapter.setPassword(srvAppserverPassword);
        return xaConnectionFactoryWrapper.wrapConnectionFactory(adapter);
    }
}

package no.nav.syfo.config;

import com.ibm.mq.jms.MQQueue;
import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;

import javax.jms.*;

import static com.ibm.mq.constants.CMQC.MQENC_NATIVE;
import static com.ibm.msg.client.jms.JmsConstants.*;
import static com.ibm.msg.client.wmq.common.CommonConstants.*;

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
    @Value("${srv.username}")
    private String serviceuserUsername;
    @Value("${srv.password}")
    private String serviceuserPassword;

    @Bean(name = "serviceVarselDestination")
    public Queue serviceVarselDestination(@Value("${servicevarsel.queuename}") String servicevarselQueueName) throws JMSException {
        return new MQQueue(servicevarselQueueName);
    }

    @Bean(name = "tredjepartsvarselVarselDestination")
    public Queue tredjepartsVarselDestination(@Value("${tredjepartsvarsel.queuename}") String tredjepartsvarselQueueName) throws JMSException {
        return new MQQueue(tredjepartsvarselQueueName);
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
    public ConnectionFactory connectionFactory() throws Exception {
        JmsConnectionFactory connectionFactory =  JmsFactoryFactory.getInstance(WMQ_PROVIDER).createXAConnectionFactory();
        connectionFactory.setIntProperty(WMQ_CONNECTION_MODE, WMQ_CM_CLIENT);
        connectionFactory.setStringProperty(WMQ_QUEUE_MANAGER, gatewayName);
        connectionFactory.setStringProperty(WMQ_HOST_NAME, gatewayHostname);
        connectionFactory.setIntProperty(WMQ_PORT, gatewayPort);
        connectionFactory.setStringProperty(WMQ_CHANNEL, channelName);
        connectionFactory.setIntProperty(WMQ_CCSID, UTF_8_WITH_PUA);
        connectionFactory.setIntProperty(JMS_IBM_ENCODING, MQENC_NATIVE);
        connectionFactory.setIntProperty(JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA);
        connectionFactory.setBooleanProperty(USER_AUTHENTICATION_MQCSP, true);
        connectionFactory.setStringProperty(USERID, serviceuserUsername);
        connectionFactory.setStringProperty(PASSWORD, serviceuserPassword);
        return connectionFactory;
    }
}

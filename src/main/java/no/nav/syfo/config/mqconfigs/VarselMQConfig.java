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
import javax.jms.Queue;

import static java.lang.System.getProperty;

//@Configuration
public class VarselMQConfig {


}

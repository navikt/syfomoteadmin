package no.nav.syfo.config.mq.mock;

import org.slf4j.*;
import org.springframework.jms.*;
import org.springframework.jms.core.*;

import javax.jms.*;

public class JmsTemplateMock extends JmsTemplate {
    private static final Logger log = LoggerFactory.getLogger(JmsTemplateMock.class);

    private String name;

    public JmsTemplateMock(String name) {
        super(new ConnectionFactoryMock());
        this.name = name;
    }

    @Override
    public void send(MessageCreator messageCreator) throws JmsException {
        log.info("Sender melding til {}", name);
        try {
            TextMessage message = (TextMessage) messageCreator.createMessage(new SessionMock());
            log.info("Call id: {}", message.getStringProperty("callId"));
            log.info("Text:\n{}", message.getText());
        } catch (JMSException e) {
            throw new UncategorizedJmsException(e);
        }
    }
}

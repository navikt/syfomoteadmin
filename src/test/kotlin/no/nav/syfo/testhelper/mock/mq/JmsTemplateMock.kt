package no.nav.syfo.testhelper.mock.mq

import org.slf4j.LoggerFactory
import org.springframework.jms.JmsException
import org.springframework.jms.UncategorizedJmsException
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessageCreator
import javax.jms.JMSException
import javax.jms.TextMessage

class JmsTemplateMock(
    private val name: String
) : JmsTemplate(ConnectionFactoryMock()) {
    @Throws(JmsException::class)
    override fun send(messageCreator: MessageCreator) {
        log.info("Sender melding til {}", name)
        try {
            val message = messageCreator.createMessage(SessionMock()) as TextMessage
            log.info("Call id: {}", message.getStringProperty("callId"))
            log.info("Text:\n{}", message.text)
        } catch (e: JMSException) {
            throw UncategorizedJmsException(e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(JmsTemplateMock::class.java)
    }
}

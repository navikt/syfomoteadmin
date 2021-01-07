package no.nav.syfo.oversikthendelse

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class OversikthendelseProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    fun sendOversikthendelse(key: String, kOversikthendelse: KOversikthendelse) {
        try {
            kafkaTemplate.send(
                OVERSIKTHENDELSE_TOPIC,
                key,
                kOversikthendelse
            ).get()
            log.info("Legger oversikthendelse med id {} på kø for enhet {}", kOversikthendelse.hendelseId, kOversikthendelse.enhetId)
        } catch (e: Exception) {
            log.error("Feil ved sending av oversikthendelse", e)
            throw RuntimeException("Feil ved sending av oversikthendelse", e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(OversikthendelseProducer::class.java)
        const val OVERSIKTHENDELSE_TOPIC = "aapen-syfo-oversikthendelse-v1"
    }
}

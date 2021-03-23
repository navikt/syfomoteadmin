package no.nav.syfo.config

import no.nav.syfo.oversikthendelse.OversikthendelseProducer
import org.junit.ClassRule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.utils.KafkaTestUtils

@Configuration
@EnableKafka
class KafkaTestConfig {
    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        return DefaultKafkaProducerFactory(KafkaTestUtils.producerProps(embeddedKafka))
    }

    companion object {
        @ClassRule
        var embeddedKafka = EmbeddedKafkaBroker(1, true, OversikthendelseProducer.OVERSIKTHENDELSE_TOPIC)
    }
}

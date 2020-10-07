package no.nav.syfo.config

import no.nav.syfo.kafka.producer.OversikthendelseProducer
import org.junit.ClassRule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.test.rule.KafkaEmbedded
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
        var embeddedKafka = KafkaEmbedded(1, true, OversikthendelseProducer.OVERSIKTHENDELSE_TOPIC)
    }
}

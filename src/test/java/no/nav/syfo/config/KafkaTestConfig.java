package no.nav.syfo.config;

import org.junit.ClassRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.rule.KafkaEmbedded;

import static no.nav.syfo.kafka.producer.OversikthendelseProducer.OVERSIKTHENDELSE_TOPIC;
import static org.springframework.kafka.test.utils.KafkaTestUtils.producerProps;

@Configuration
@EnableKafka
public class KafkaTestConfig {

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, OVERSIKTHENDELSE_TOPIC);

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerProps(embeddedKafka));
    }
}
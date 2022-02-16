package no.nav.syfo.config.mq

import no.nav.syfo.testhelper.mock.mq.JmsTemplateMock
import org.springframework.context.annotation.*
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.core.JmsTemplate

@Configuration
@EnableJms
@Profile("local")
class JmsTestConfig {
    @Bean(name = ["stoppvarselqueue"])
    fun stoppvarselqueue(): JmsTemplate {
        return JmsTemplateMock("stoppvarselqueue")
    }

    @Bean(name = ["servicevarselqueue"])
    fun servicevarselqueue(): JmsTemplate {
        return JmsTemplateMock("servicevarselqueue")
    }

    @Bean(name = ["tredjepartsvarselqueue"])
    fun tredjepartsvarselqueue(): JmsTemplate {
        return JmsTemplateMock("tredjepartsvarselqueue")
    }
}

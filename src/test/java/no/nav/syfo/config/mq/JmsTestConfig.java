package no.nav.syfo.config.mq;

import no.nav.syfo.config.mq.mock.JmsTemplateMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableJms
@Profile({"local"})
public class JmsTestConfig {

    @Bean(name = "oppgavehenvendelsequeue")
    public JmsTemplate oppgaveHenvendelseQueue() {
        return new JmsTemplateMock("oppgaveHenvendelseQueue");
    }

    @Bean(name = "opprettVarselQueue")
    public JmsTemplate opprettvarselqueue() {
        return new JmsTemplateMock("opprettvarselqueue");
    }

    @Bean(name = "stoppvarselqueue")
    public JmsTemplate stoppvarselqueue() {
        return new JmsTemplateMock("stoppvarselqueue");
    }

    @Bean(name = "servicevarselqueue")
    public JmsTemplate servicevarselqueue() {
        return new JmsTemplateMock("servicevarselqueue");
    }

    @Bean(name = "tredjepartsvarselqueue")
    public JmsTemplate tredjepartsvarselqueue() {
        return new JmsTemplateMock("tredjepartsvarselqueue");
    }
}

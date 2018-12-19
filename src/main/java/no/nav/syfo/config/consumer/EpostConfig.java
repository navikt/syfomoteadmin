package no.nav.syfo.config.consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static java.lang.System.getProperty;

@Configuration
public class EpostConfig {
    @Bean
    public JavaMailSender javaMailSender(){
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setDefaultEncoding("UTF-8");
        mailSender.setHost(getProperty("SMTPSERVER_HOST"));
        mailSender.setPort(Integer.parseInt(getProperty("SMTPSERVER_PORT")));
        return mailSender;
    }
}

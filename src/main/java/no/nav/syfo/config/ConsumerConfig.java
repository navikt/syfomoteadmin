package no.nav.syfo.config;

import no.nav.dialogarena.aktor.AktorConfig;
import no.nav.syfo.config.consumer.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        AktorConfig.class,
        EpostConfig.class,
        BrukerprofilConfig.class,
        PersonConfig.class,
        SykefravaersoppfoelgingConfig.class,
        EgenAnsattConfig.class
})
public class ConsumerConfig {

}

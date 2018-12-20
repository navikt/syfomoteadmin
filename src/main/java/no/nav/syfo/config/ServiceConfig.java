package no.nav.syfo.config;

import no.nav.syfo.service.HistorikkService;
import no.nav.syfo.service.TilgangService;
import no.nav.syfo.service.*;
import no.nav.syfo.service.mq.MqHenvendelseService;
import no.nav.syfo.service.mq.MqOppgaveVarselService;
import no.nav.syfo.service.mq.MqStoppRevarslingService;
import no.nav.syfo.service.varselinnhold.ArbeidsgiverVarselService;
import no.nav.syfo.service.varselinnhold.SykmeldtVarselService;
import no.nav.syfo.service.varselinnhold.VeilederVarselService;
import no.nav.syfo.util.DatoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public AktoerService aktoerService() {
        return new AktoerService();
    }

    @Bean
    public MotedeltakerService motedeltakerService() {
        return new MotedeltakerService();
    }

    @Bean
    public MoteService moteService() {
        return new MoteService();
    }

    @Bean
    public NaermesteLedersMoterService naermesteLedersMoterService() {
        return new NaermesteLedersMoterService();
    }

    @Bean
    public EpostService epostService() {
        return new EpostService();
    }

    @Bean
    public VeilederService veilederService() {
        return new VeilederService();
    }

    @Bean
    public DatoService timeService() {
        return new DatoService();
    }

    @Bean
    public HendelseService hendelseService() {
        return new HendelseService();
    }

    @Bean
    public BrukerprofilService brukerprofilService() {
        return new BrukerprofilService();
    }

    @Bean
    public EgenAnsattService egenAnsattService() {
        return new EgenAnsattService();
    }

    @Bean
    public ArbeidsgiverVarselService motedeltakerArbeidsgiverVarselInnholdService() {
        return new ArbeidsgiverVarselService();
    }

    @Bean
    public SykmeldtVarselService motedeltakerAktoerIdVarselInnholdService() {
        return new SykmeldtVarselService();
    }

    @Bean
    public VeilederVarselService veilederVarselInnholdService() {
        return new VeilederVarselService();
    }

    @Bean
    public MqStoppRevarslingService stoppRevarslingService() {
        return new MqStoppRevarslingService();
    }

    @Bean
    public MetricsService metricsService() {
        return new MetricsService();
    }

    @Bean
    public SykefravaersoppfoelgingService sykefravaersoppfoelgingService() {
        return new SykefravaersoppfoelgingService();
    }

    @Bean
    public OrganisasjonService organisasjonService() {
        return new OrganisasjonService();
    }

    @Bean
    public ArenaService arenaService() {
        return new ArenaService();
    }

    @Bean
    public DkifService dkifService() {
        return new DkifService();
    }

    @Bean
    public NorgService norgService() {
        return new NorgService();
    }

    @Bean
    public MqOppgaveVarselService oppgaveVarselService() {
        return new MqOppgaveVarselService();
    }

    @Bean
    public ServiceVarselService serviceVarselService() {
        return new ServiceVarselService();
    }

    @Bean
    public MqHenvendelseService henvendelseService() {
        return new MqHenvendelseService();
    }

    @Bean
    public FeedService feedService() {
        return new FeedService();
    }

    @Bean
    public BrukertilgangService brukertilgangService() {
        return new BrukertilgangService();
    }

    @Bean
    public TilgangService tilgangService() {
        return new TilgangService();
    }

    @Bean
    public HistorikkService historikkService() {
        return new HistorikkService();
    }
}


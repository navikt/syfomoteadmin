package no.nav.syfo.config.consumer;

import no.nav.syfo.service.ws.LogErrorHandler;
import no.nav.syfo.service.ws.STSClientConfig;
import no.nav.syfo.service.ws.WsClient;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.HentNaermesteLedersAnsattListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.SykefravaersoppfoelgingV1;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.meldinger.WSHentNaermesteLedersAnsattListeRequest;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.meldinger.WSHentNaermesteLedersAnsattListeResponse;
import org.apache.cxf.frontend.ClientProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static java.util.Collections.singletonList;
import static no.nav.syfo.util.OIDCUtil.leggTilOnBehalfOfOutInterceptorForOIDC;

@Configuration
public class SykefravaersoppfoelgingConfig {

    public static final String MOCK_KEY = "sykefravaersoppfoelging.withmock";
    @Value("${sykefravaersoppfoelging.v1.endpointurl}")
    private String serviceUrl;

    private SykefravaersoppfoelgingV1 portUser;

    @Bean(name = "sykefravaersoppfoelgingV1")
    @Primary
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    public SykefravaersoppfoelgingV1 sykefravaersoppfoelgingV1() {
        SykefravaersoppfoelgingV1 port = factory();
        STSClientConfig.configureRequestSamlTokenOnBehalfOfOidc(port);
        this.portUser = port;
        return port;
    }

    @Bean(name = "sykefravaersoppfoelgingV1SystemBruker")
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    public SykefravaersoppfoelgingV1 sykefravaersoppfoelgingV1Systembruker() {
        SykefravaersoppfoelgingV1 port = factory();
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }

    @SuppressWarnings("unchecked")
    private SykefravaersoppfoelgingV1 factory() {
        return new WsClient<SykefravaersoppfoelgingV1>()
                .createPort(serviceUrl, SykefravaersoppfoelgingV1.class, singletonList(new LogErrorHandler()));
    }

    public WSHentNaermesteLedersAnsattListeResponse hentNaermesteLedersAnsattListe(WSHentNaermesteLedersAnsattListeRequest request, String OIDCToken) throws HentNaermesteLedersAnsattListeSikkerhetsbegrensning {
        leggTilOnBehalfOfOutInterceptorForOIDC(ClientProxy.getClient(portUser), OIDCToken);
        return portUser.hentNaermesteLedersAnsattListe(request);
    }
}

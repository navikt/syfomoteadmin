package no.nav.syfo.service;

import no.nav.security.oidc.OIDCConstants;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.config.consumer.SykefravaersoppfoelgingConfig;
import no.nav.syfo.domain.model.Ansatt;
import no.nav.syfo.oidc.OIDCIssuer;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.HentNaermesteLedersAnsattListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.SykefravaersoppfoelgingV1;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.informasjon.WSAnsatt;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.informasjon.WSNaermesteLederStatus;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.meldinger.WSHentNaermesteLedersAnsattListeResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static java.time.LocalDate.of;
import static java.util.Arrays.asList;
import static no.nav.syfo.testhelper.OidcTestHelper.lagOIDCValidationContextEkstern;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR;
import static no.nav.syfo.util.TestUtil.biForEach;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SykefravaersoppfoelgingServiceTest {

    @Mock
    private OIDCRequestContextHolder oidcRequestContextHolder;
    @Mock
    private SykefravaersoppfoelgingV1 sykefravaersoppfoelgingV1;
    @Mock
    private SykefravaersoppfoelgingConfig sykefravaersoppfoelgingConfig;
    @InjectMocks
    private SykefravaersoppfoelgingService sykefravaersoppfoelgingService;

    @Before
    public void setup() {
        when(oidcRequestContextHolder.getRequestAttribute(OIDCConstants.OIDC_VALIDATION_CONTEXT)).thenReturn(lagOIDCValidationContextEkstern(ARBEIDSTAKER_FNR));
    }

    @After
    public void cleanUp() {
        loggUtAlle(oidcRequestContextHolder);
    }

    @Test
    public void hentNaermesteLedersAnsattListe() throws Exception {
        List<WSAnsatt> wsAnsatte = asList(new WSAnsatt()
                        .withAktoerId("AktoerId1")
                        .withOrgnummer("Orgnummer1")
                        .withNaermesteLederId(1L)
                        .withNavn("Navn1")
                        .withHarNySykmelding(false)
                        .withNaermesteLederStatus(new WSNaermesteLederStatus()
                                .withErAktiv(true)
                                .withAktivFom(of(2017, 3, 2))),
                new WSAnsatt()
                        .withAktoerId("AktoerId2")
                        .withOrgnummer("Orgnummer2")
                        .withNaermesteLederId(2L)
                        .withNavn("Navn2")
                        .withHarNySykmelding(true)
                        .withNaermesteLederStatus(new WSNaermesteLederStatus()
                                .withErAktiv(false)
                                .withAktivFom(of(2017, 1, 2))
                                .withAktivTom(of(2017, 2, 2))));

        when(sykefravaersoppfoelgingConfig.hentNaermesteLedersAnsattListe(any(), any())).thenReturn(
                new WSHentNaermesteLedersAnsattListeResponse().withAnsattListe(wsAnsatte));

        List<Ansatt> ansatte = sykefravaersoppfoelgingService.hentNaermesteLedersAnsattListe("nlAktoerId", OIDCIssuer.EKSTERN);

        biForEach(wsAnsatte, ansatte, (wsAnsatt, ansatt) -> {
            assertThat(ansatt.aktoerId).isEqualTo(wsAnsatt.getAktoerId());
            assertThat(ansatt.orgnummer).isEqualTo(wsAnsatt.getOrgnummer());
            assertThat(ansatt.naermesteLederId).isEqualTo(wsAnsatt.getNaermesteLederId());
            assertThat(ansatt.navn).isEqualTo(wsAnsatt.getNavn());
            assertThat(ansatt.harNySykmelding).isEqualTo(wsAnsatt.isHarNySykmelding());
            assertThat(ansatt.naermesteLederStatus.erAktiv).isEqualTo(wsAnsatt.getNaermesteLederStatus().isErAktiv());
            assertThat(ansatt.naermesteLederStatus.aktivFom).isEqualTo(wsAnsatt.getNaermesteLederStatus().getAktivFom());
            assertThat(ansatt.naermesteLederStatus.aktivTom).isEqualTo(wsAnsatt.getNaermesteLederStatus().getAktivTom());
        });
    }

    @Test(expected = RuntimeException.class)
    public void hentNaermesteLedersAnsattListeIkkeTilgang() throws Exception {
        when(sykefravaersoppfoelgingConfig.hentNaermesteLedersAnsattListe(any(), any())).thenThrow(new HentNaermesteLedersAnsattListeSikkerhetsbegrensning());
        sykefravaersoppfoelgingService.hentNaermesteLedersAnsattListe("nlAktoerId", OIDCIssuer.EKSTERN);
    }
}

package no.nav.syfo.config.mocks;

import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.*;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.informasjon.WSAnsatt;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.informasjon.WSNaermesteLeder;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.informasjon.WSNaermesteLederStatus;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.meldinger.*;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;

public class SykefravaersoppfoelgingV1Mock implements SykefravaersoppfoelgingV1 {

    @Override
    public WSHentNaermesteLederListeResponse hentNaermesteLederListe(WSHentNaermesteLederListeRequest request) {
        return new WSHentNaermesteLederListeResponse()
                .withNaermesteLederListe(
                        asList(
                                new WSNaermesteLeder()
                                        .withNavn("Test Testesen")
                                        .withEpost("test@testesen.no")
                                        .withMobil("12345678")
                                        .withNaermesteLederAktoerId("1234125")
                                        .withNaermesteLederId(16L)
                                        .withOrgnummer("123456789")));
    }

    @Override
    public WSHentSykeforlopperiodeResponse hentSykeforlopperiode(WSHentSykeforlopperiodeRequest request) throws HentSykeforlopperiodeSikkerhetsbegrensning {
        return null;
    }

    @Override
    public WSHentHendelseListeResponse hentHendelseListe(WSHentHendelseListeRequest request) throws HentHendelseListeSikkerhetsbegrensning {
        return null;
    }

    @Override
    public WSHentNaermesteLedersHendelseListeResponse hentNaermesteLedersHendelseListe(WSHentNaermesteLedersHendelseListeRequest request) throws HentNaermesteLedersHendelseListeSikkerhetsbegrensning {
        return null;
    }

    @Override
    public WSBerikNaermesteLedersAnsattBolkResponse berikNaermesteLedersAnsattBolk(WSBerikNaermesteLedersAnsattBolkRequest request) throws BerikNaermesteLedersAnsattBolkSikkerhetsbegrensning {
        return null;
    }

    @Override
    public void ping() {
    }

    @Override
    public WSHentNaermesteLederResponse hentNaermesteLeder(WSHentNaermesteLederRequest request) {
        return new WSHentNaermesteLederResponse()
                .withNaermesteLeder(
                        new WSNaermesteLeder()
                                .withNaermesteLederStatus(new WSNaermesteLederStatus()
                                        .withAktivFom(now().minusDays(22)))
                                .withNavn("Test Testesen")
                                .withOrgnummer("378")
                                .withEpost("frode@bjelland.no")
                                .withMobil("123456789"));
    }

    @Override
    public WSHentNaermesteLedersAnsattListeResponse hentNaermesteLedersAnsattListe(WSHentNaermesteLedersAnsattListeRequest request) {
        return new WSHentNaermesteLedersAnsattListeResponse().withAnsattListe(asList(
                new WSAnsatt()
                        .withNaermesteLederStatus(new WSNaermesteLederStatus().withErAktiv(true).withAktivFom(now().minusDays(22)))
                        .withHarNySykmelding(true)
                        .withAktoerId("1000028253764")
                        .withNaermesteLederId(345)
                        .withNavn("Test Testesen")
                        .withOrgnummer("123456789"),
                new WSAnsatt()
                        .withNaermesteLederStatus(new WSNaermesteLederStatus().withErAktiv(true).withAktivFom(now().minusDays(22)))
                        .withHarNySykmelding(true)
                        .withAktoerId("1000028253762")
                        .withNaermesteLederId(234)
                        .withNavn("Test Testesen")
                        .withOrgnummer("123456789")
        ));
    }

}

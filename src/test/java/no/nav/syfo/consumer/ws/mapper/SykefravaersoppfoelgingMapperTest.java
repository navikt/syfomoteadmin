package no.nav.syfo.consumer.ws.mapper;

import no.nav.syfo.domain.model.Ansatt;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.informasjon.WSAnsatt;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.informasjon.WSNaermesteLederStatus;
import org.junit.Test;

import static java.time.LocalDate.of;
import static no.nav.syfo.mappers.SykefravaersoppfoelgingMapper.ws2Ansatt;
import static no.nav.syfo.util.MapUtil.map;
import static org.assertj.core.api.Assertions.assertThat;

public class SykefravaersoppfoelgingMapperTest {
    @Test
    public void ws2Ansatt() {
        WSAnsatt wsAnsatt = new WSAnsatt()
                .withAktoerId("AktoerId")
                .withOrgnummer("Orgnummer")
                .withNaermesteLederId(1L)
                .withNavn("Navn")
                .withHarNySykmelding(false)
                .withNaermesteLederStatus(new WSNaermesteLederStatus()
                        .withErAktiv(true)
                        .withAktivFom(of(2017, 3, 2))
                );

        Ansatt ansatt = map(wsAnsatt, ws2Ansatt);

        assertThat(ansatt.aktoerId).isEqualTo("AktoerId");
        assertThat(ansatt.orgnummer).isEqualTo("Orgnummer");
        assertThat(ansatt.naermesteLederId).isEqualTo(1L);
        assertThat(ansatt.navn).isEqualTo("Navn");
        assertThat(ansatt.harNySykmelding).isFalse();
        assertThat(ansatt.naermesteLederStatus.erAktiv).isTrue();
        assertThat(ansatt.naermesteLederStatus.aktivFom).isEqualTo(of(2017, 3, 2));
        assertThat(ansatt.naermesteLederStatus.aktivTom).isNull();
    }
}
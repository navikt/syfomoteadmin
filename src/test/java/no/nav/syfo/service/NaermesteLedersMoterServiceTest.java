package no.nav.syfo.service;

import no.nav.syfo.domain.model.Ansatt;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.NaermesteLederStatus;
import no.nav.syfo.domain.model.TidOgSted;
import no.nav.syfo.oidc.OIDCIssuer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NaermesteLedersMoterServiceTest {
    @Mock
    private SykefravaersoppfoelgingService sykefravaersoppfoelgingService;
    @Mock
    private MoteService moteService;
    @InjectMocks
    private NaermesteLedersMoterService naermesteLedersMoterService;

    @Test
    public void hentNaermeteLedersMoter() throws Exception {
        when(sykefravaersoppfoelgingService.hentNaermesteLedersAnsattListe("nlAktoerId", OIDCIssuer.EKSTERN)).thenReturn(
                new ArrayList<>(
                        asList(
                                new Ansatt()
                                        .aktoerId("aktoerId1")
                                        .orgnummer("orgnummer1")
                                        .naermesteLederStatus(new NaermesteLederStatus()
                                                .aktivFom(LocalDate.of(2017, 3, 2))),
                                new Ansatt()
                                        .aktoerId("aktoerId2")
                                        .orgnummer("orgnummer2")
                                        .naermesteLederStatus(new NaermesteLederStatus()
                                                .aktivFom(LocalDate.of(2017, 3, 2))))));

        when(moteService.findMoterByBrukerAktoerIdOgAGOrgnummer("aktoerId1", "orgnummer1")).thenReturn(
                new ArrayList<>(asList(
                        new Mote()
                                .id(1L)
                                .alternativer(asList(
                                        new TidOgSted()
                                                .tid(LocalDateTime.of(2017, 3, 1, 12, 13)),
                                        new TidOgSted()
                                                .tid(LocalDateTime.of(2017, 3, 2, 12, 13))
                                )),
                        new Mote()
                                .id(3L)
                                .alternativer(asList(
                                        new TidOgSted()
                                                .tid(LocalDateTime.of(2017, 3, 1, 12, 13)),
                                        new TidOgSted()
                                                .tid(LocalDateTime.of(2017, 3, 2, 12, 13))
                                )))));

        when(moteService.findMoterByBrukerAktoerIdOgAGOrgnummer("aktoerId2", "orgnummer2")).thenReturn(
                new ArrayList<>(singletonList(
                        new Mote()
                                .id(2L)
                                .alternativer(asList(
                                        new TidOgSted()
                                                .tid(LocalDateTime.of(2017, 3, 1, 12, 13)),
                                        new TidOgSted()
                                                .tid(LocalDateTime.of(2017, 3, 2, 12, 13))
                                )))));

        List<Mote> moter = naermesteLedersMoterService.hentNaermesteLedersMoter("nlAktoerId");

        assertThat(moter).hasSize(3);
        assertThat(moter.get(0).id).isEqualTo(1L);
        assertThat(moter.get(1).id).isEqualTo(3L);
        assertThat(moter.get(2).id).isEqualTo(2L);
    }

    @Test
    public void hentNaermeteLedersMoterToGamleMoter() throws Exception {
        when(sykefravaersoppfoelgingService.hentNaermesteLedersAnsattListe("nlAktoerId", OIDCIssuer.EKSTERN)).thenReturn(
                new ArrayList<>(
                        asList(
                                new Ansatt()
                                        .aktoerId("aktoerId1")
                                        .orgnummer("orgnummer1")
                                        .naermesteLederStatus(new NaermesteLederStatus()
                                                .aktivFom(LocalDate.of(2017, 3, 2))),
                                new Ansatt()
                                        .aktoerId("aktoerId2")
                                        .orgnummer("orgnummer2")
                                        .naermesteLederStatus(new NaermesteLederStatus()
                                                .aktivFom(LocalDate.of(2017, 3, 3))))));

        when(moteService.findMoterByBrukerAktoerIdOgAGOrgnummer("aktoerId1", "orgnummer1")).thenReturn(
                new ArrayList<>(asList(
                        new Mote()
                                .id(1L)
                                .alternativer(asList(
                                        new TidOgSted()
                                                .tid(LocalDateTime.of(2017, 2, 28, 12, 13)),
                                        new TidOgSted()
                                                .tid(LocalDateTime.of(2017, 3, 1, 12, 13))
                                )),
                        new Mote()
                                .id(3L)
                                .alternativer(asList(
                                        new TidOgSted()
                                                .tid(LocalDateTime.of(2017, 3, 1, 12, 13)),
                                        new TidOgSted()
                                                .tid(LocalDateTime.of(2017, 3, 2, 12, 13))
                                )))));

        when(moteService.findMoterByBrukerAktoerIdOgAGOrgnummer("aktoerId2", "orgnummer2")).thenReturn(
                new ArrayList<>(singletonList(
                        new Mote()
                                .id(2L)
                                .alternativer(asList(
                                        new TidOgSted()
                                                .tid(LocalDateTime.of(2017, 3, 1, 12, 13)),
                                        new TidOgSted()
                                                .tid(LocalDateTime.of(2017, 3, 2, 12, 13))
                                )))));

        List<Mote> moter = naermesteLedersMoterService.hentNaermesteLedersMoter("nlAktoerId");

        assertThat(moter).hasSize(1);
        assertThat(moter.get(0).id).isEqualTo(3L);
    }

    @Test
    public void hentNaermeteLedersMoterIngenAnsatte() throws Exception {
        when(sykefravaersoppfoelgingService.hentNaermesteLedersAnsattListe("nlAktoerId", OIDCIssuer.EKSTERN)).thenReturn(emptyList());

        List<Mote> moter = naermesteLedersMoterService.hentNaermesteLedersMoter("nlAktoerId");

        assertThat(moter).isEmpty();
    }

    @Test
    public void hentNaermeteLedersMoterIngenMoter() throws Exception {
        when(sykefravaersoppfoelgingService.hentNaermesteLedersAnsattListe("nlAktoerId", OIDCIssuer.EKSTERN)).thenReturn(
                asList(
                        new Ansatt()
                                .aktoerId("aktoerId1")
                                .orgnummer("orgnummer1"),
                        new Ansatt()
                                .aktoerId("aktoerId2")
                                .orgnummer("orgnummer2")));

        when(moteService.findMoterByBrukerAktoerIdOgAGOrgnummer(startsWith("aktoerId"), startsWith("orgnummer"))).thenReturn(emptyList());

        List<Mote> moter = naermesteLedersMoterService.hentNaermesteLedersMoter("nlAktoerId");

        assertThat(moter).isEmpty();
    }
}

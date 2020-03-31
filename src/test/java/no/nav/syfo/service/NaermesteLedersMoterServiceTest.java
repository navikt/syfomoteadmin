package no.nav.syfo.service;

import no.nav.syfo.domain.model.*;
import no.nav.syfo.narmesteleder.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.*;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NaermesteLedersMoterServiceTest {
    @Mock
    private NarmesteLederConsumer narmesteLederConsumer;
    @Mock
    private MoteService moteService;
    @InjectMocks
    private NaermesteLedersMoterService naermesteLedersMoterService;

    @Test
    public void hentNaermeteLedersMoter() throws Exception {
        when(narmesteLederConsumer.narmestelederRelasjoner("nlAktoerId")).thenReturn(
                new ArrayList<>(
                        asList(
                                new NarmesteLederRelasjon(
                                        "aktoerId1",
                                        "orgnummer1",
                                        null,
                                        null,
                                        null,
                                        LocalDate.of(2017, 3, 2),
                                        false,
                                        false,
                                        null
                                ),
                                new NarmesteLederRelasjon(
                                        "aktoerId2",
                                        "orgnummer2",
                                        null,
                                        null,
                                        null,
                                        LocalDate.of(2017, 3, 2),
                                        false,
                                        false,
                                        null
                                ))));

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
        when(narmesteLederConsumer.narmestelederRelasjoner("nlAktoerId")).thenReturn(
                new ArrayList<>(
                        asList(
                                new NarmesteLederRelasjon(
                                        "aktoerId1",
                                        "orgnummer1",
                                        null,
                                        null,
                                        null,
                                        LocalDate.of(2017, 3, 2),
                                        false,
                                        false,
                                        null
                                ),
                                new NarmesteLederRelasjon(
                                        "aktoerId2",
                                        "orgnummer2",
                                        null,
                                        null,
                                        null,
                                        LocalDate.of(2017, 3, 3),
                                        false,
                                        false,
                                        null
                                ))));

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
        when(narmesteLederConsumer.narmestelederRelasjoner("nlAktoerId")).thenReturn(emptyList());

        List<Mote> moter = naermesteLedersMoterService.hentNaermesteLedersMoter("nlAktoerId");

        assertThat(moter).isEmpty();
    }

    @Test
    public void hentNaermeteLedersMoterIngenMoter() throws Exception {
        when(narmesteLederConsumer.narmestelederRelasjoner("nlAktoerId")).thenReturn(
                new ArrayList<>(
                        asList(
                                new NarmesteLederRelasjon(
                                        "aktoerId1",
                                        "orgnummer1",
                                        null,
                                        null,
                                        null,
                                        LocalDate.of(2017, 3, 2),
                                        false,
                                        false,
                                        null
                                ),
                                new NarmesteLederRelasjon(
                                        "aktoerId2",
                                        "orgnummer2",
                                        null,
                                        null,
                                        null,
                                        LocalDate.of(2017, 3, 3),
                                        false,
                                        false,
                                        null
                                ))));

        when(moteService.findMoterByBrukerAktoerIdOgAGOrgnummer(startsWith("aktoerId"), startsWith("orgnummer"))).thenReturn(emptyList());

        List<Mote> moter = naermesteLedersMoterService.hentNaermesteLedersMoter("nlAktoerId");

        assertThat(moter).isEmpty();
    }
}

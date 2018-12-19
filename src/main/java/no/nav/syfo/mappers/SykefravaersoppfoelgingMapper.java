package no.nav.syfo.mappers;

import no.nav.syfo.domain.model.Ansatt;
import no.nav.syfo.domain.model.NaermesteLederStatus;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.informasjon.WSAnsatt;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.informasjon.WSNaermesteLederStatus;

import java.util.function.Function;

import static no.nav.syfo.util.MapUtil.map;

public final class SykefravaersoppfoelgingMapper {

    private static final Function<WSNaermesteLederStatus, NaermesteLederStatus> ws2NaermesteLederStatus = wsNaermesteLederStatus -> new NaermesteLederStatus()
            .erAktiv(wsNaermesteLederStatus.isErAktiv())
            .aktivFom(wsNaermesteLederStatus.getAktivFom())
            .aktivTom(wsNaermesteLederStatus.getAktivTom());

    public static final Function<WSAnsatt, Ansatt> ws2Ansatt = wsAnsatt -> new Ansatt()
            .aktoerId(wsAnsatt.getAktoerId())
            .orgnummer(wsAnsatt.getOrgnummer())
            .naermesteLederId(wsAnsatt.getNaermesteLederId())
            .navn(wsAnsatt.getNavn())
            .harNySykmelding(wsAnsatt.isHarNySykmelding())
            .naermesteLederStatus(map(wsAnsatt.getNaermesteLederStatus(), ws2NaermesteLederStatus));
}

package no.nav.syfo.repository.mapper;

import no.nav.syfo.domain.model.NaermesteLeder;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.informasjon.WSNaermesteLeder;

import java.util.function.Function;

import static org.apache.commons.lang3.text.WordUtils.capitalize;

public class NaermesteLederMapper {

    public static Function<WSNaermesteLeder, NaermesteLeder> ws2naermesteleder = wsNaermesteLeder ->
            new NaermesteLeder()
                    .naermesteLederId(wsNaermesteLeder.getNaermesteLederId())
                    .naermesteLederAktoerId(wsNaermesteLeder.getNaermesteLederAktoerId())
                    .navn(capitalize(wsNaermesteLeder.getNavn()))
                    .epost(wsNaermesteLeder.getEpost())
                    .tlf(wsNaermesteLeder.getMobil())
                    .orgnummer(wsNaermesteLeder.getOrgnummer())
                    .aktivFom(wsNaermesteLeder.getNaermesteLederStatus().getAktivFom())
                    .aktivTom(wsNaermesteLeder.getNaermesteLederStatus().getAktivTom());
}

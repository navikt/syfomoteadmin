package no.nav.syfo.repository.mapper;

import no.nav.syfo.domain.model.TidOgSted;

import java.util.function.Function;

public class PTidStedMapper {
    public static Function<no.nav.syfo.repository.model.PTidOgSted, TidOgSted> p2TidOgSted = pTidOgSted -> new TidOgSted()
            .id(pTidOgSted.id)
            .tid(pTidOgSted.tid)
            .created(pTidOgSted.created)
            .sted(pTidOgSted.sted);
}

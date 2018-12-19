package no.nav.syfo.repository.mapper;

import no.nav.syfo.domain.model.EpostVedlegg;
import no.nav.syfo.repository.model.PEpostVedlegg;

import java.util.function.Function;

public class PEpostMapper {

    public static Function<EpostVedlegg, PEpostVedlegg> epostvedlegg2p = vedlegg -> new PEpostVedlegg()
            .epostId(vedlegg.epostId)
            .innhold(vedlegg.innhold)
            .type(vedlegg.type.name());

}

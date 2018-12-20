package no.nav.syfo.repository.mapper;

import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.MoteStatus;

import java.util.function.Function;

public class PMoteMapper {

    public static Function<no.nav.syfo.repository.model.PMote, Mote> p2Mote = pMote -> new Mote()
            .id(pMote.id)
            .uuid(pMote.uuid)
            .navEnhet(pMote.navEnhet)
            .opprettetAv(pMote.opprettetAv)
            .eier(pMote.eier)
            .status(MoteStatus.valueOf(pMote.status))
            .opprettetTidspunkt(pMote.created);
}

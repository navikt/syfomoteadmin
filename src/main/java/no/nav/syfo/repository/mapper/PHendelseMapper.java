package no.nav.syfo.repository.mapper;

import no.nav.syfo.domain.model.HendelseMoteStatusEndret;
import no.nav.syfo.domain.model.HendelsesType;
import no.nav.syfo.domain.model.MoteStatus;
import no.nav.syfo.repository.model.PHendelseMoteStatusEndret;

import java.util.function.Function;

public class PHendelseMapper {

    public static Function<PHendelseMoteStatusEndret, HendelseMoteStatusEndret> p2motestatusendrethendelse = pHendelseMoteStatusEndret ->
            new HendelseMoteStatusEndret()
                    .status(MoteStatus.valueOf(pHendelseMoteStatusEndret.status))
                    .id(pHendelseMoteStatusEndret.id)
                    .moteId(pHendelseMoteStatusEndret.moteId)
                    .inntruffetdato(pHendelseMoteStatusEndret.inntruffetdato)
                    .opprettetAv(pHendelseMoteStatusEndret.opprettetAv)
                    .type(HendelsesType.MOTESTATUS_ENDRET);

}

package no.nav.syfo.api.mappers;

import no.nav.syfo.api.domain.*;
import no.nav.syfo.domain.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.MapUtil.mapNullable;
import static no.nav.syfo.util.MoterUtil.finnAktoerFraMote;

public class RSMoteMapper {

    public static Function<TidOgSted, RSTidOgSted> tidOgSted2rs = tidOgSted ->
            new RSTidOgSted()
                    .id(tidOgSted.id)
                    .sted(tidOgSted.sted)
                    .tid(tidOgSted.tid)
                    .created(tidOgSted.created)
                    .valgt(tidOgSted.valgt);

    public static Function<Motedeltaker, RSMotedeltaker> motedeltaker2rs = motedeltaker -> {
        if (motedeltaker instanceof MotedeltakerArbeidsgiver) {
            MotedeltakerArbeidsgiver arbeidsgiver = (MotedeltakerArbeidsgiver) motedeltaker;
            return new RSMotedeltaker()
                    .deltakerUuid(arbeidsgiver.uuid)
                    .navn(arbeidsgiver.navn)
                    .svartidspunkt(arbeidsgiver.svartTidspunkt)
                    .svar(mapListe(arbeidsgiver.tidOgStedAlternativer, tidOgSted2rs))
                    .type("arbeidsgiver")
                    .epost(arbeidsgiver.epost)
                    .orgnummer(arbeidsgiver.orgnummer);
        }

        MotedeltakerAktorId aktoer = (MotedeltakerAktorId) motedeltaker;
        return new RSMotedeltaker()
                .deltakerUuid(aktoer.uuid)
                .navn(aktoer.navn)
                .svartidspunkt(aktoer.svartTidspunkt)
                .svar(mapListe(aktoer.tidOgStedAlternativer, tidOgSted2rs))
                .type("Bruker")
                .fnr(aktoer.fnr);
    };

    public static Function<List<Hendelse>, LocalDateTime> finnSisteBekreftelseTidspunkt = hendelser -> hendelser.stream()
            .filter(hendelse -> hendelse instanceof HendelseMoteStatusEndret)
            .map(hendelse -> (HendelseMoteStatusEndret) hendelse)
            .filter(hendelse -> hendelse.status.equals(MoteStatus.BEKREFTET))
            .sorted((o1, o2) -> o2.inntruffetdato.compareTo(o1.inntruffetdato))
            .map(hendelseMoteStatusEndret -> hendelseMoteStatusEndret.inntruffetdato)
            .findFirst()
            .orElse(null);

    public static Function<List<Hendelse>, LocalDateTime> finnSisteEndretHendelseAvVeileder = hendelser -> hendelser.stream()
            .filter(hendelse -> hendelse instanceof HendelseVarselVeileder)
            .map(hendelse -> (HendelseVarselVeileder) hendelse)
            .sorted((o1, o2) -> o2.inntruffetdato.compareTo(o1.inntruffetdato))
            .map(hendelseMoteEndretAvVeileder -> hendelseMoteEndretAvVeileder.inntruffetdato)
            .findFirst()
            .orElse(null);


    public static Function<Mote, RSMote> mote2rs = mote ->
            new RSMote()
                    .id(mote.id)
                    .aktorId(finnAktoerFraMote(mote).aktorId)
                    .moteUuid(mote.uuid)
                    .navEnhet(mote.navEnhet)
                    .opprettetAv(mote.opprettetAv)
                    .eier(mote.eier)
                    .status(mote.status.name())
                    .opprettetTidspunkt(mote.opprettetTidspunkt)
                    .bekreftetTidspunkt(mapNullable(mote.hendelser, finnSisteBekreftelseTidspunkt))
                    .bekreftetAlternativ(mapNullable(mote.valgtTidOgSted, tidOgSted2rs))
                    .alternativer(mapListe(mote.alternativer, tidOgSted2rs))
                    .deltakere(mapListe(mote.motedeltakere, motedeltaker2rs));

}

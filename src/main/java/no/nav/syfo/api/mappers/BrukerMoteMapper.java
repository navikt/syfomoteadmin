package no.nav.syfo.api.mappers;

import no.nav.syfo.api.domain.bruker.BrukerMote;
import no.nav.syfo.api.domain.bruker.BrukerMotedeltaker;
import no.nav.syfo.api.domain.bruker.BrukerTidOgSted;
import no.nav.syfo.domain.model.*;

import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.MapUtil.mapNullable;

public class BrukerMoteMapper {
    private static Function<Motedeltaker, String> motedeltaker2Orgnummer = motedeltaker -> {
        if (motedeltaker instanceof MotedeltakerArbeidsgiver) {
            return ((MotedeltakerArbeidsgiver) motedeltaker).orgnummer;
        }
        return null;
    };

    private static Function<Motedeltaker, String> motedeltaker2Navn = motedeltaker -> {
        if (motedeltaker instanceof MotedeltakerArbeidsgiver) {
            return ((MotedeltakerArbeidsgiver) motedeltaker).navn;
        }
        return null;
    };

    private static Function<Motedeltaker, String> motedeltakerAktoerId2AktoerId = motedeltaker -> {
        if (motedeltaker instanceof MotedeltakerAktorId) {
            return ((MotedeltakerAktorId) motedeltaker).aktorId;
        }
        return null;
    };

    private static Function<TidOgSted, BrukerTidOgSted> tidOgSted2brukerTidOgSted = ts -> new BrukerTidOgSted()
            .id(ts.id)
            .tid(ts.tid)
            .created(ts.created)
            .sted(ts.sted)
            .valgt(ts.valgt);

    private static Function<Motedeltaker, BrukerMotedeltaker> motedeltaker2motedeltaker = motedeltaker ->
            new BrukerMotedeltaker()
                    .deltakerUuid(motedeltaker.uuid)
                    .svartidspunkt(motedeltaker.svartTidspunkt)
                    .aktoerId(mapNullable(motedeltaker, motedeltakerAktoerId2AktoerId))
                    .svar(mapListe(motedeltaker.tidOgStedAlternativer, tidOgSted2brukerTidOgSted))
                    .navn(mapNullable(motedeltaker, motedeltaker2Navn))
                    .orgnummer(mapNullable(motedeltaker, motedeltaker2Orgnummer))
                    .type(motedeltaker.motedeltakertype);

    public static Function<Mote, BrukerMote> mote2BrukerMote = mote ->
            new BrukerMote()
                    .moteUuid(mote.uuid)
                    .status(mote.status.name())
                    .opprettetTidspunkt(mote.opprettetTidspunkt)
                    .bekreftetTidspunkt(ofNullable(mote.valgtTidOgSted).map(TidOgSted::tid).orElse(null))
                    .bekreftetAlternativ(mapNullable(mote.valgtTidOgSted, tidOgSted2brukerTidOgSted.andThen(ts -> ts.valgt(true))))
                    .deltakere(mapListe(mote.motedeltakere, motedeltaker2motedeltaker))
                    .alternativer(mapListe(mote.alternativer, tidOgSted2brukerTidOgSted));
}

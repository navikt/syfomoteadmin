package no.nav.syfo.util;

import no.nav.syfo.domain.model.TidOgSted;
import no.nav.syfo.domain.model.Veileder;
import no.nav.syfo.repository.model.PEpost;
import no.nav.syfo.repository.model.PEpostVedlegg;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static no.nav.syfo.util.EpostInnholdUtil.BUNN;
import static no.nav.syfo.util.OutlookEventUtil.IcsStatus.*;
import static no.nav.syfo.util.OutlookEventUtil.icsInnhold;

public class EpostInnholdVeilederUtil {

    public static PEpost opprettetEpostVeileder(Veileder veileder, List<TidOgSted> alternativer) {
        return new PEpost()
                .emne(opprettetEpostVeilederEmne())
                .innhold(opprettetEpostVeilederInnhold())
                .vedlegg(opprettetEpostVeilederIcs(veileder.mote.uuid, alternativer));
    }

    public static PEpost bekreftetEpostVeileder(Veileder veileder, TidOgSted alternativ) {
        return new PEpost()
                .emne(bekreftetEpostVeilederEmne())
                .innhold(bekreftetEpostVeilederInnhold())
                .vedlegg(singletonList(new PEpostVedlegg()
                        .innhold(bekreftetEpostVeilederIcs(veileder.mote.uuid + alternativ.id, alternativ.sted, alternativ.tid))
                        .type("ICS")
                ));
    }

    public static PEpost flereTidspunktEpostVeileder(Veileder veileder, List<TidOgSted> alternativer) {
        return new PEpost()
                .emne(flereTidspunktEpostVeilederEmne())
                .innhold(flereTidspunktEpostVeilederInnhold())
                .vedlegg(flereTidspunktEpostVeilederIcs(veileder.mote.uuid, alternativer));
    }

    public static PEpost avvistEpostVeileder(Veileder veileder, List<TidOgSted> alternativer) {
        PEpost epostInnhold = new PEpost()
                .emne(avvistEpostVeilederEmne())
                .innhold(avvistEpostVeilederInnhold());

        alternativer.forEach(alternativ -> epostInnhold.vedlegg(singletonList(new PEpostVedlegg()
                .innhold(avvistEpostVeilederIcs(veileder.mote.uuid + alternativ.id, alternativ.sted, alternativ.tid))
                .type("ICS")))
        );

        return epostInnhold;
    }

    private static List<PEpostVedlegg> opprettetEpostVeilederIcs(String uuid, List<TidOgSted> alternativer) {
        List<PEpostVedlegg> epostVedlegg = new ArrayList<>();
        for (TidOgSted alternativ : alternativer) {
            epostVedlegg.add(new PEpostVedlegg()
                    .innhold(icsInnhold(uuid + alternativ.id, alternativ.tid, opprettetEpostVeilederEmne(), alternativ.sted, empty(), TENTATIVE))
                    .type("ICS")
            );
        }
        return epostVedlegg;
    }

    private static String opprettetEpostVeilederEmne() {
        return "Foreløpig tidspunkt for dialogmøte";
    }

    private static String opprettetEpostVeilederInnhold() {
        return EpostInnholdUtil.TOPP +
                "<p>Vedlagt finner du foreløpig tidspunkt for dialogmøte. Klikk på kalenderfilen for å reservere det aktuelle tidspunktet i din kalender.\n" +
                BUNN;
    }

    private static String bekreftetEpostVeilederIcs(String uuid, String sted, LocalDateTime dato) {
        return icsInnhold(uuid, dato, bekreftetEpostVeilederEmne(), sted, empty(), CONFIRMED);
    }

    private static String bekreftetEpostVeilederEmne() {
        return "Bekreftet tidspunkt for dialogmøte";
    }

    private static String bekreftetEpostVeilederInnhold() {
        return EpostInnholdUtil.TOPP +
                "<p>Dette tidspunktet er bekreftet for møte</p>" +
                BUNN;
    }

    private static String flereTidspunktEpostVeilederEmne() {
        return "Flere tidspunkt for dialogmøte";
    }

    private static String flereTidspunktEpostVeilederInnhold() {
        return EpostInnholdUtil.TOPP +
                "<p>Det er satt av flere foreløpige tidspunkter for dialogmøte. Klikk på de vedlagte kalenderfilene for å reservere tidspunktene i din kalender.\n" +
                BUNN;
    }

    private static List<PEpostVedlegg> flereTidspunktEpostVeilederIcs(String uuid, List<TidOgSted> alternativer) {
        List<PEpostVedlegg> epostVedlegg = new ArrayList<>();
        for (TidOgSted alternativ : alternativer) {
            epostVedlegg.add(new PEpostVedlegg()
                    .innhold(icsInnhold(uuid + alternativ.id, alternativ.tid, flereTidspunktEpostVeilederEmne(), alternativ.sted, empty(), TENTATIVE))
                    .type("ICS")
            );
        }
        return epostVedlegg;
    }

    private static String avvistEpostVeilederIcs(String uuid, String sted, LocalDateTime dato) {
        return icsInnhold(uuid, dato, avvistEpostVeilederEmne(), sted, empty(), CANCELLED);
    }

    private static String avvistEpostVeilederEmne() {
        return "Tidspunkt for dialogmøte er ikke lenger aktuelt";
    }

    private static String avvistEpostVeilederInnhold() {
        return EpostInnholdUtil.TOPP +
                "<p>Foreløpig tidspunkt for dialogmøte er ikke lenger aktuelt. Klikk på kalenderfilen(e) for å fjerne foreløpig tidspunkt fra kalenderen din.</p>" +
                BUNN;
    }


}

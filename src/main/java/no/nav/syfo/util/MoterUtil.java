package no.nav.syfo.util;

import no.nav.syfo.domain.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;

public class MoterUtil {

    public static boolean erSisteSvarMottatt(List<Motedeltaker> motedeltakere) {
        return motedeltakere.stream().noneMatch(m -> m.svartTidspunkt == null);
    }

    public static MotedeltakerAktorId finnAktoerFraMote(Mote Mote) {
        return Mote.motedeltakere.stream()
                .filter(motedeltaker -> motedeltaker instanceof MotedeltakerAktorId)
                .map(motedeltaker -> (MotedeltakerAktorId) motedeltaker)
                .findFirst().orElseThrow(() -> new RuntimeException("Fant ikke Aktoer knyttet til møtet!"));
    }

    public static Optional<LocalDateTime> hentSisteSvartidspunkt(Mote Mote) {
        return Mote.motedeltakere.stream()
                .filter(motedeltaker -> motedeltaker.svartTidspunkt != null)
                .map(motedeltaker -> motedeltaker.svartTidspunkt)
                .sorted(reverseOrder()).findFirst();
    }

    public static List<TidOgSted> filtrerBortAlternativerSomAlleredeErLagret(List<TidOgSted> nyeAlternativer, Mote Mote) {
        return nyeAlternativer.stream()
                .filter(nyttAlternativ -> Mote.alternativer.stream()
                        .noneMatch(alternativ -> alternativ.tid.equals(nyttAlternativ.tid)))
                .collect(toList());
    }

}

package no.nav.syfo.util

import no.nav.syfo.domain.model.Mote
import no.nav.syfo.narmesteleder.NarmesteLederRelasjon
import java.time.LocalDate


fun narmesteLederForMeeting(narmesteLedere: List<NarmesteLederRelasjon>, mote: Mote): NarmesteLederRelasjon? {
    val wantedOrgnummer = mote.arbeidsgiver().orgnummer
    val moteOpprettetDate = mote.opprettetTidspunkt?.toLocalDate() ?: LocalDate.now()

    val narmesteLeder = narmesteLedere.filter {
        isLeaderForOrgnummerAndAktivFomBeforeDate(it, wantedOrgnummer, moteOpprettetDate)
    }.maxBy { it.aktivFom }

    return narmesteLeder ?: throw RuntimeException("Kunne ikke finne leder for det gitte møtet")
}

private fun isLeaderForOrgnummerAndAktivFomBeforeDate(leder: NarmesteLederRelasjon, orgnummer: String, fom: LocalDate): Boolean {
    return leder.orgnummer == orgnummer && leder.aktivFom.isBefore(fom)
}

package no.nav.syfo.util

import no.nav.syfo.consumer.narmesteleder.NarmesteLederRelasjonDTO
import no.nav.syfo.domain.model.Mote
import java.time.LocalDate

fun narmesteLederForMeeting(narmesteLedere: List<NarmesteLederRelasjonDTO>, mote: Mote): NarmesteLederRelasjonDTO {
    val wantedOrgnummer = mote.arbeidsgiver().orgnummer
    val moteOpprettetDate = mote.opprettetTidspunkt?.toLocalDate() ?: LocalDate.now()

    val narmesteLeder = narmesteLedere.filter {
        isLeaderForOrgnummerAndAktivFomBeforeDate(it, wantedOrgnummer, moteOpprettetDate)
    }.maxByOrNull { it.aktivFom }

    return narmesteLeder ?: throw RuntimeException("Kunne ikke finne leder for det gitte møtet")
}

private fun isLeaderForOrgnummerAndAktivFomBeforeDate(
    leder: NarmesteLederRelasjonDTO,
    virksomhetsnummer: String,
    fom: LocalDate
): Boolean {
    return leder.virksomhetsnummer == virksomhetsnummer && leder.aktivFom.isBefore(fom)
}

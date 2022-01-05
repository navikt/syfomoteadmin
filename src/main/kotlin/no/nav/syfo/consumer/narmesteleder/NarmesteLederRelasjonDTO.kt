package no.nav.syfo.consumer.narmesteleder

import java.time.LocalDate
import java.time.LocalDateTime

data class NarmesteLederRelasjonDTO(
    val uuid: String,
    val arbeidstakerPersonIdentNumber: String,
    val virksomhetsnavn: String?,
    val virksomhetsnummer: String,
    val narmesteLederPersonIdentNumber: String,
    val narmesteLederTelefonnummer: String,
    val narmesteLederEpost: String,
    val narmesteLederNavn: String?,
    val aktivFom: LocalDate,
    val aktivTom: LocalDate?,
    val arbeidsgiverForskutterer: Boolean?,
    val timestamp: LocalDateTime,
    val status: String,
)

fun List<NarmesteLederRelasjonDTO>.relasjonerWhereIdentIsInnbygger(innbyggerIdent: String): List<NarmesteLederRelasjonDTO> {
    return this.filter {
        it.arbeidstakerPersonIdentNumber == innbyggerIdent
    }
}

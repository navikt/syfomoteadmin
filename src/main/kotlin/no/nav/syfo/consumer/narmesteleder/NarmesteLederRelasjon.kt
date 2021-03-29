package no.nav.syfo.consumer.narmesteleder

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class NarmesteLederRelasjon(
    val aktorId: String,
    val orgnummer: String,
    val narmesteLederAktorId: String,
    val narmesteLederTelefonnummer: String?,
    val narmesteLederEpost: String?,
    val aktivFom: LocalDate,
    val aktivTom: LocalDate?,
    val arbeidsgiverForskutterer: Boolean?,
    val skrivetilgang: Boolean,
    val tilganger: List<Tilgang>,
    val navn: String? = null
)

enum class Tilgang {
    SYKMELDING,
    SYKEPENGESOKNAD,
    MOTE,
    OPPFOLGINGSPLAN
}
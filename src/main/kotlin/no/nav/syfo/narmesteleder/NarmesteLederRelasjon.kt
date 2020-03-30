package no.nav.syfo.narmesteleder

import java.time.LocalDate

class NarmesteLederRelasjon(
        val aktorId: String,
        val orgnummer: String,
        val narmesteLederAktorId: String? = null,
        val narmesteLederTelefonnummer: String? = null,
        val narmesteLederEpost: String? = null,
        val aktivFom: LocalDate,
        val arbeidsgiverForskutterer: Boolean = false,
        val skrivetilgang: Boolean = false,
        val tilganger: List<Tilgang>? = null
)

enum class Tilgang {
    SYKMELDING, SYKEPENGESOKNAD, MOTE, OPPFOLGINGSPLAN
}
package no.nav.syfo.narmesteleder

import java.io.Serializable

data class Ansatt(
        val aktoerId: String,
        val virksomhetsnummer: String
) : Serializable
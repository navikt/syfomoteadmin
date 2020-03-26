package no.nav.syfo.axsys

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AxsysTilgangerResponse(
        val enheter: List<AxsysEnhet>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AxsysEnhet(
        val enhetId: String,
        val navn: String
)

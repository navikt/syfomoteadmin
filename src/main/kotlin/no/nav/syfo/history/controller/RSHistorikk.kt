package no.nav.syfo.history.controller

import java.time.LocalDateTime

data class RSHistorikk(
    val opprettetAv: String,
    val tekst: String,
    val tidspunkt: LocalDateTime
)

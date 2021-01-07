package no.nav.syfo.controller.internad.person

data class RSKontaktinfo (
    val tlf: String? = null,
    val epost: String? = null,
    val reservasjon: RSReservasjon?
)

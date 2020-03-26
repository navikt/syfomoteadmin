package no.nav.syfo.controller.internad.veileder

import java.io.Serializable

data class RSEnhet(
        val enhetId: String,
        var navn: String
) : Serializable

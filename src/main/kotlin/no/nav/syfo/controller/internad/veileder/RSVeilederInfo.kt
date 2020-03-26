package no.nav.syfo.controller.internad.veileder

import java.io.Serializable

data class RSVeilederInfo(
        val navn: String,
        val ident: String
) : Serializable

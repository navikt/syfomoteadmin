package no.nav.syfo.controller.internad.veileder

import java.io.Serializable

data class RSEnheter(
        val enhetliste: List<RSEnhet>
) : Serializable

package no.nav.syfo.util

import no.nav.syfo.api.domain.RSMote
import java.time.LocalDateTime


fun erRSMotePassert(rsMote: RSMote): Boolean {
    val nyesteAlternativ = rsMote.alternativer.maxBy { it.created }
    return nyesteAlternativ!!.tid.isBefore(LocalDateTime.now())
}

package no.nav.syfo.util

import no.nav.syfo.domain.model.Mote
import java.time.LocalDateTime


fun newestTidFromMoteAlternativ(mote: Mote): LocalDateTime {
    val alternativWithBiggestTid = mote.alternativer.maxBy { it.tid }
    return alternativWithBiggestTid!!.tid
}

fun moterAfterGivenDate(moter: List<Mote>, date: LocalDateTime): List<Mote> {
    return moter.filter { newestTidFromMoteAlternativ(it).isAfter(date) }
}

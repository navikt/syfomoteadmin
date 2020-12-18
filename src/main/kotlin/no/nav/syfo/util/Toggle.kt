package no.nav.syfo.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class Toggle(
    @Value("\${toggle.enable.batch.paaminnelse:true}") private val toggleBatchPaaminelse: Boolean
) {
    fun toggleBatchPaaminelse(): Boolean {
        return toggleBatchPaaminelse
    }
}

package no.nav.syfo.metric

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.syfo.util.DatoService
import org.springframework.stereotype.Controller
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@Controller
class Metric @Inject constructor(
    private val registry: MeterRegistry
) {
    fun countEvent(navn: String) {
        registry.counter(
            addPrefix(navn),
            Tags.of("type", "info")
        ).increment()
    }

    fun countOutgoingReponses(navn: String, statusCode: Int) {
        registry.counter(
            addPrefix(navn),
            Tags.of(
                "type", "info",
                "status", statusCode.toString()
            )
        ).increment()
    }

    fun tellEndepunktKall(navn: String) {
        registry.counter(
            addPrefix(navn),
            Tags.of("type", "info")
        ).increment()
    }

    fun tellHttpKall(kode: Int) {
        registry.counter(
            addPrefix("httpstatus"),
            Tags.of(
                "type", "info",
                "kode", kode.toString())
        ).increment()
    }

    fun tellTredjepartVarselSendt(type: String?) {
        registry.counter(addPrefix("tredjepartvarsler_sendt"), Tags.of("type", "info", "varseltype", type))
            .increment()
    }

    fun reportAntallDagerSiden(tidspunkt: LocalDateTime, navn: String) {
        val dagerIMellom = DatoService.dagerMellom(tidspunkt.toLocalDate(), LocalDate.now())
        registry.counter(
            addPrefix(navn + "FraOpprettetMote"),
            Tags.of("type", "info")
        ).increment(dagerIMellom.toDouble())
    }

    private fun addPrefix(navn: String): String {
        return "syfomoteadmin_$navn"
    }
}

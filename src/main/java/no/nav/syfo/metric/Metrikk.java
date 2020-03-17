package no.nav.syfo.metric;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import java.time.LocalDateTime;

import static java.time.LocalDate.now;
import static no.nav.syfo.util.DatoService.dagerMellom;

@Controller
public class Metrikk {

    private final MeterRegistry registry;

    @Inject
    public Metrikk(MeterRegistry registry) {
        this.registry = registry;
    }

    public void countEvent(String navn) {
        registry.counter(
                addPrefix(navn),
                Tags.of("type", "info")
        ).increment();
    }

    public void countOutgoingReponses(String navn, Integer statusCode) {
        registry.counter(
                addPrefix(navn),
                Tags.of(
                        "type", "info",
                        "status", statusCode.toString()
                )
        ).increment();
    }

    public void tellEndepunktKall(String navn) {
        registry.counter(
                addPrefix(navn),
                Tags.of("type", "info")
        ).increment();
    }

    public void tellHttpKall(int kode) {
        registry.counter(
                addPrefix("httpstatus"),
                Tags.of(
                        "type", "info",
                        "kode", String.valueOf(kode)
                )
        ).increment();
    }

    public void tellTredjepartVarselSendt(String type) {
        registry.counter(addPrefix("tredjepartvarsler_sendt"), Tags.of("type", "info", "varseltype", type))
                .increment();
    }

    public void reportAntallDagerSiden(LocalDateTime tidspunkt, String navn) {
        int dagerIMellom = dagerMellom(tidspunkt.toLocalDate(), now());

        registry.counter(
                addPrefix(navn + "FraOpprettetMote"),
                Tags.of("type", "info")
        ).increment(dagerIMellom);
    }

    private String addPrefix(String navn) {
        String METRIKK_PREFIX = "syfomoteadmin_";
        return METRIKK_PREFIX + navn;
    }
}

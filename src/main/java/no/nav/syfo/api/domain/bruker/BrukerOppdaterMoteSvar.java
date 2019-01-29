package no.nav.syfo.api.domain.bruker;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Data
@Accessors(fluent = true)
public class BrukerOppdaterMoteSvar {
    public LocalDateTime svartidspunkt = now();
}

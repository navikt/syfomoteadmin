package no.nav.syfo.api.domain.bruker;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class BrukerOppdaterMoteSvar {
    public LocalDateTime svartidspunkt = now();
}

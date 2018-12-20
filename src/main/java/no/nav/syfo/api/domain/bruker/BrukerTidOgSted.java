package no.nav.syfo.api.domain.bruker;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class BrukerTidOgSted {
    public Long id;
    public LocalDateTime tid;
    public LocalDateTime created;
    public String sted;
    public boolean valgt;
}

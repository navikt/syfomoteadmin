package no.nav.syfo.api.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class RSTidOgSted {
    public Long id;
    public LocalDateTime tid;
    public LocalDateTime created;
    public String sted;
    public boolean valgt;
}

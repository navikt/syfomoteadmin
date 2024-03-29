package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class TidOgSted {
    public Long id;
    public long moteId;
    public LocalDateTime tid;
    public LocalDateTime created;
    public String sted;
    public boolean valgt;
}

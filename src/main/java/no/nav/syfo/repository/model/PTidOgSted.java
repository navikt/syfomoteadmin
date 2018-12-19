package no.nav.syfo.repository.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class PTidOgSted {
    public Long id;
    public long moteId;
    public LocalDateTime tid;
    public String sted;
    public LocalDateTime created;
}

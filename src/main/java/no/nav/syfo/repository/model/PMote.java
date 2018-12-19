package no.nav.syfo.repository.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class PMote {
    public Long id;
    public String uuid;
    public String opprettetAv;
    public String eier;
    public String navEnhet;
    public Long valgtTidStedId;
    public String status;
    public LocalDateTime created;
}

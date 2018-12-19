package no.nav.syfo.repository.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class PHendelse {
    public Long id;
    public LocalDateTime inntruffetdato;
    public String type;
    public String opprettetAv;
}

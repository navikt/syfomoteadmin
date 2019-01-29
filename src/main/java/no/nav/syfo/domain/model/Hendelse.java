package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
public class Hendelse {
    public Long id;
    public HendelsesType type;
    public LocalDateTime inntruffetdato;
    public String opprettetAv;
}

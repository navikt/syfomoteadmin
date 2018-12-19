package no.nav.syfo.rest.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class RSHistorikk {
    public String opprettetAv;
    public String tekst;
    public LocalDateTime tidspunkt;
}

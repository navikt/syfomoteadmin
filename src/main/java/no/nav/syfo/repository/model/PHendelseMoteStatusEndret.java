package no.nav.syfo.repository.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class PHendelseMoteStatusEndret extends PHendelse {
    public Long id;
    public LocalDateTime inntruffetdato;
    public String type;
    public String opprettetAv;

    public long moteId;
    public String status;
}


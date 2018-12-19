package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
public class HendelseMoteStatusEndret extends Hendelse {
    public MoteStatus status;
    public long moteId;

    public HendelseMoteStatusEndret id(Long id) {
        this.id = id;
        return this;
    }
    public HendelseMoteStatusEndret type(HendelsesType type) {
        this.type = type;
        return this;
    }
    public HendelseMoteStatusEndret inntruffetdato(LocalDateTime inntruffetdato) {
        this.inntruffetdato = inntruffetdato;
        return this;
    }
    public HendelseMoteStatusEndret opprettetAv(String opprettetAv) {
        this.opprettetAv = opprettetAv;
        return this;
    }

}

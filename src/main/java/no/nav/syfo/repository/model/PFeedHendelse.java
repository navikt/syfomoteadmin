package no.nav.syfo.repository.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

import static no.nav.syfo.repository.model.PFeedHendelse.FeedHendelseType.ALLE_SVAR_MOTTATT;

@Data
@Accessors(fluent = true)
public class PFeedHendelse {
    public long id;
    public long moteId;
    public String uuid;
    public LocalDateTime created;
    public String sistEndretAv;
    public String type;

    public String status() {
        if (this.type.equals(ALLE_SVAR_MOTTATT.name())) {
            return "IKKE_STARTET";
        } else {
            return "FERDIG";
        }
    }


    public enum FeedHendelseType {
        ALLE_SVAR_MOTTATT,
        AVBRUTT,
        FLERE_TIDSPUNKT,
        BEKREFTET,
    }
}

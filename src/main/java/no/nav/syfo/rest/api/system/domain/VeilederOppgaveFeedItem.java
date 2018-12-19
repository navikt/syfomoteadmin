package no.nav.syfo.rest.api.system.domain;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class VeilederOppgaveFeedItem {
    public String uuid;
    public String type;
    public String tildeltIdent;
    public String tildeltEnhet;
    public String lenke;
    public String fnr;
    public String virksomhetsnummer;
    public LocalDateTime created;
    public LocalDateTime sistEndret;
    public String sistEndretAv;
    public String status;


    public enum FeedHendelseType {
        ALLE_SVAR_MOTTATT,
        AVBRUTT,
        FLERE_TIDSPUNKT,
        BEKREFTET,
    }
}


package no.nav.syfo.api.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
public class RSMote {
    public long id;
    public String moteUuid;
    public String opprettetAv;
    public String aktorId;
    public String status;
    public String fnr;
    public LocalDateTime opprettetTidspunkt;
    public LocalDateTime bekreftetTidspunkt;
    public String navEnhet;
    public String eier;
    public List<RSMotedeltaker> deltakere;
    public RSTidOgSted bekreftetAlternativ;
    public List<RSTidOgSted> alternativer = new ArrayList<>();
    public LocalDateTime sistEndret;
    public boolean trengerBehandling;
}


package no.nav.syfo.api.domain.bruker;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class BrukerMote {
    public String moteUuid;
    public String status;
    public String fnr;
    public LocalDateTime opprettetTidspunkt;
    public LocalDateTime bekreftetTidspunkt;
    public List<BrukerMotedeltaker> deltakere = new ArrayList<>();
    public BrukerTidOgSted bekreftetAlternativ;
    public List<BrukerTidOgSted> alternativer = new ArrayList<>();
}

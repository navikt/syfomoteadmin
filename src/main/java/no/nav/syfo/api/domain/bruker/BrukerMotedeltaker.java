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
public class BrukerMotedeltaker {

    public transient String deltakerUuid;
    public transient String aktoerId;
    public String navn;
    public String orgnummer;
    public String type;
    public LocalDateTime svartidspunkt;
    public List<BrukerTidOgSted> svar = new ArrayList<>();
}

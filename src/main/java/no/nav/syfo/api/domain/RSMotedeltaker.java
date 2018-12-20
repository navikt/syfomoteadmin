package no.nav.syfo.api.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class RSMotedeltaker {
    public String deltakerUuid;
    public String navn;
    public String fnr;
    public String orgnummer;
    public String epost;
    public String type;
    public LocalDateTime svartidspunkt;
    public List<RSTidOgSted> svar = new ArrayList<>();
}

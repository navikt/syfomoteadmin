package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class Ansatt {
    public String aktoerId;
    public String orgnummer;
    public long naermesteLederId;
    public String navn;
    public boolean harNySykmelding;
    public NaermesteLederStatus naermesteLederStatus;
}

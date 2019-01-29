package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Ansatt {
    public String aktoerId;
    public String orgnummer;
    public long naermesteLederId;
    public String navn;
    public boolean harNySykmelding;
    public NaermesteLederStatus naermesteLederStatus;
}

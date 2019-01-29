package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(fluent = true)
public class NaermesteLeder {
    public Long naermesteLederId;
    public String naermesteLederAktoerId;
    public LocalDate aktivFom;
    public LocalDate aktivTom;
    public String orgnummer;
    public String navn;
    public String epost;
    public String tlf;
}

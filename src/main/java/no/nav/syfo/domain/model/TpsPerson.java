package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class TpsPerson {

    public String navn;
    public boolean skjermetBruker = false;
    public boolean erKode6 = false;
}

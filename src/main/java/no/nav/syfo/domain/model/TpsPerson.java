package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class TpsPerson {
    public String navn;
    public boolean skjermetBruker = false;
    public boolean erKode6 = false;
}

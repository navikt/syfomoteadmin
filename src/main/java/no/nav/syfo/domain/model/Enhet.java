package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Enhet {
    public String enhetId;
    public String navn;
}

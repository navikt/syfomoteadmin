package no.nav.syfo.api.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class RSEpostInnhold {
    public String emne;
    public String innhold;
}

package no.nav.syfo.api.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class RSTilgang {
    public boolean harTilgang;
    public String begrunnelse;
}

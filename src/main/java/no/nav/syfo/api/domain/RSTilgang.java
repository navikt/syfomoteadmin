package no.nav.syfo.api.domain;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class RSTilgang {
    public boolean harTilgang;
    public String begrunnelse;
}

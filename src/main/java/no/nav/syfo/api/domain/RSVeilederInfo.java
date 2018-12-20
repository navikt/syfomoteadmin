package no.nav.syfo.api.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class RSVeilederInfo {

    public String navn;
    public String ident;
}

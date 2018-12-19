package no.nav.syfo.rest.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class RSBruker {

    public String navn;
    public RSKontaktinfo kontaktinfo = new RSKontaktinfo();
}

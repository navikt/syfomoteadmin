package no.nav.syfo.api.domain.nyttmoterequest;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class RSNyttAlternativ {
    public String tid;
    public String sted;
}

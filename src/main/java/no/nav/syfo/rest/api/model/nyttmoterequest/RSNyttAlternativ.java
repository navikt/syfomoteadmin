package no.nav.syfo.rest.api.model.nyttmoterequest;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class RSNyttAlternativ {
    public String tid;
    public String sted;
}

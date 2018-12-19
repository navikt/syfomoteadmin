package no.nav.syfo.rest.api.model.nyttmoterequest;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class RSNyttMoteRequest {
    public List<RSNyttAlternativ> alternativer = new ArrayList<>();
    public String fnr;
    public String orgnummer;
    public String navn;
    public String epost;
    public String navEnhet;

}

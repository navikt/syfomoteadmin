package no.nav.syfo.api.domain.nyttmoterequest;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
public class RSNyttMoteRequest {
    public List<RSNyttAlternativ> alternativer = new ArrayList<>();
    public String fnr;
    public String orgnummer;
    public String navn;
    public String epost;
    public String navEnhet;
}

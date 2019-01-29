package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.syfo.domain.interfaces.Varsel;

@Data
@Accessors(fluent = true)
public class Veileder implements Varsel {
    public String navn;
    public String epost;
    public String userId;
    public Mote mote;
}

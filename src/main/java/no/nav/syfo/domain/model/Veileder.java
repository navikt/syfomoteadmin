package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Veileder {
    public String navn;
    public String epost;
    public String userId;
    public Mote mote;
}

package no.nav.syfo.api.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class RSKontaktinfo {
    public String tlf;
    public String epost;
    public RSReservasjon reservasjon = new RSReservasjon();
}

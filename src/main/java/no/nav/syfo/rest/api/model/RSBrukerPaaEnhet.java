package no.nav.syfo.rest.api.model;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class RSBrukerPaaEnhet {
    public String fnr;
    public boolean skjermetEllerEgenAnsatt;
}

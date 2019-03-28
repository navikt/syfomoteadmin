package no.nav.syfo.api.domain;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class RSBrukerPaaEnhet {
    public String fnr;
    public Skjermingskode skjermetEllerEgenAnsatt;

    public enum Skjermingskode {
        DISKRESJONSMERKET,
        EGEN_ANSATT,
        INGEN
    }
}

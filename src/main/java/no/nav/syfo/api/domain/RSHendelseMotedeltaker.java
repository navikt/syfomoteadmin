package no.nav.syfo.api.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class RSHendelseMotedeltaker {
    public String kanal;
    public String adresse;
    public String varseltype;
    public String resultat;

}

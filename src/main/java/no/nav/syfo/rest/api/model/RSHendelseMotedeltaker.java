package no.nav.syfo.rest.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class RSHendelseMotedeltaker {

    public String kanal;
    public String adresse;
    public String varseltype;
    public String resultat;

}

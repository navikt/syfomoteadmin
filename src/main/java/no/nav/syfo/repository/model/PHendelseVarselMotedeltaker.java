package no.nav.syfo.repository.model;


import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class PHendelseVarselMotedeltaker extends PHendelse {
    public Long id;
    public LocalDateTime inntruffetdato;
    public String type;
    public String opprettetAv;

    public Long motedeltakerId;
    public String kanal;
    public String adresse;
    public String resultat;
    public String varseltype;
}


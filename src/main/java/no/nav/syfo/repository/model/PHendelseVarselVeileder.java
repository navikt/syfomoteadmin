package no.nav.syfo.repository.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class PHendelseVarselVeileder extends PHendelse {
    public Long id;
    public LocalDateTime inntruffetdato;
    public String type;
    public String opprettetAv;

    public long moteId;
    public String kanal;
    public String veilederident;
    public String varseltype;
}


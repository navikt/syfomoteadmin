package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
public class HendelseVarselVeileder extends Hendelse {
    public Mote Mote;
    public Kanal kanal;
    public String adresse;
    public String veilederident;
    public String varseltype;


    public HendelseVarselVeileder id(Long id) {
        this.id = id;
        return this;
    }
    public HendelseVarselVeileder type(HendelsesType type) {
        this.type = type;
        return this;
    }
    public HendelseVarselVeileder inntruffetdato(LocalDateTime inntruffetdato) {
        this.inntruffetdato = inntruffetdato;
        return this;
    }
    public HendelseVarselVeileder opprettetAv(String opprettetAv) {
        this.opprettetAv = opprettetAv;
        return this;
    }
}


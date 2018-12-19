package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
public class HendelseVarselMotedeltaker extends Hendelse {
    public Motedeltaker motedeltaker;
    public Kanal kanal;
    public String adresse;
    public Resultat resultat;
    public String varseltype;

    public enum Resultat {
        OK,
        KRR_INGEN_KONTAKTINFORMASJON,
        KRR_RESERVERT,
        KRR_UTGAATT,
        KRR_SIKKERHETSBEGRENSNING
    }

    public HendelseVarselMotedeltaker id(Long id) {
        this.id = id;
        return this;
    }
    public HendelseVarselMotedeltaker type(HendelsesType type) {
        this.type = type;
        return this;
    }
    public HendelseVarselMotedeltaker inntruffetdato(LocalDateTime inntruffetdato) {
        this.inntruffetdato = inntruffetdato;
        return this;
    }
    public HendelseVarselMotedeltaker opprettetAv(String opprettetAv) {
        this.opprettetAv = opprettetAv;
        return this;
    }

}

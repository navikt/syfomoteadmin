package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
public class MotedeltakerArbeidsgiver extends Motedeltaker {
    public String epost;
    public String orgnummer;

    public MotedeltakerArbeidsgiver id(Long id) {
        this.id = id;
        return this;
    }

    public MotedeltakerArbeidsgiver uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public MotedeltakerArbeidsgiver navn(String navn) {
        this.navn = navn;
        return this;
    }

    public MotedeltakerArbeidsgiver motedeltakertype(String motedeltakertype) {
        this.motedeltakertype = motedeltakertype;
        return this;
    }

    public MotedeltakerArbeidsgiver svartTidspunkt(LocalDateTime svartTidspunkt) {
        this.svartTidspunkt = svartTidspunkt;
        return this;
    }

    public MotedeltakerArbeidsgiver status(MotedeltakerStatus status) {
        this.status = status;
        return this;
    }

    public MotedeltakerArbeidsgiver tidOgStedAlternativer(List<TidOgSted> tidOgStedAlternativer) {
        this.tidOgStedAlternativer = tidOgStedAlternativer;
        return this;
    }
}

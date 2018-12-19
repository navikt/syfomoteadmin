package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
public class MotedeltakerAktorId extends Motedeltaker {
    public String aktorId;
    public String fnr;
    public Kontaktinfo kontaktinfo;

    public MotedeltakerAktorId id(Long id) {
        this.id = id;
        return this;
    }

    public MotedeltakerAktorId uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }
    public MotedeltakerAktorId navn(String navn) {
        this.navn = navn;
        return this;
    }
    public MotedeltakerAktorId motedeltakertype(String motedeltakertype) {
        this.motedeltakertype = motedeltakertype;
        return this;
    }
    public MotedeltakerAktorId svartTidspunkt(LocalDateTime svartTidspunkt) {
        this.svartTidspunkt = svartTidspunkt;
        return this;
    }
    public MotedeltakerAktorId status(MotedeltakerStatus status) {
        this.status = status;
        return this;
    }
    public MotedeltakerAktorId tidOgStedAlternativer(List<TidOgSted> tidOgStedAlternativer) {
        this.tidOgStedAlternativer = tidOgStedAlternativer;
        return this;
    }

}

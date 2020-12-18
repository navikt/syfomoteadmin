package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
public class Motedeltaker {
    public Long id;
    public Mote mote;
    public String uuid;
    public String navn;
    public String motedeltakertype;
    public LocalDateTime svartTidspunkt;
    public MotedeltakerStatus status;
    public List<TidOgSted> tidOgStedAlternativer = new ArrayList<>();
}


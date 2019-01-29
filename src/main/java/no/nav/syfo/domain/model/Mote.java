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
public class Mote {
    public Long id;
    public String uuid;
    public String opprettetAv;
    public String eier;
    public String navEnhet;
    public List<Hendelse> hendelser = new ArrayList<>();
    public LocalDateTime opprettetTidspunkt;
    public TidOgSted valgtTidOgSted;
    public List<TidOgSted> alternativer = new ArrayList<>();
    public List<Motedeltaker> motedeltakere = new ArrayList<>();
    public LocalDateTime sistEndret;
    public MoteStatus status;
    public Veileder veileder;

    public MotedeltakerArbeidsgiver arbeidsgiver() {
        return motedeltakere.stream()
                .filter(motedeltaker -> motedeltaker instanceof MotedeltakerArbeidsgiver)
                .map(motedeltaker -> (MotedeltakerArbeidsgiver) motedeltaker)
                .findFirst().get();
    }

    public MotedeltakerAktorId sykmeldt() {
        return motedeltakere.stream()
                .filter(motedeltaker -> motedeltaker instanceof MotedeltakerAktorId)
                .map(motedeltaker -> (MotedeltakerAktorId) motedeltaker)
                .findFirst().get();
    }
}

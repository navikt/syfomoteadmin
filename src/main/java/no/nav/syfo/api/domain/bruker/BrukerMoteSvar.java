package no.nav.syfo.api.domain.bruker;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class BrukerMoteSvar {
    public String deltakertype;
    public List<Long> valgteAlternativIder = new ArrayList<>();
}

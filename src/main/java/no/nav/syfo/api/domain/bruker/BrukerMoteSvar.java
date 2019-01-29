package no.nav.syfo.api.domain.bruker;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
public class BrukerMoteSvar {
    public String deltakertype;
    public List<Long> valgteAlternativIder = new ArrayList<>();
}

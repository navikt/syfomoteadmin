package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class EpostInnhold {
    public String emne;
    public String innhold;
    public List<EpostVedlegg> vedlegg = new ArrayList<>();
}

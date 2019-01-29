package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
public class EpostInnhold {
    public String emne;
    public String innhold;
    public List<EpostVedlegg> vedlegg = new ArrayList<>();
}

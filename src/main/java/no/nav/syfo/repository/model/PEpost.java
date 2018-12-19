package no.nav.syfo.repository.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
public class PEpost {
    public Long id;
    public String mottaker;
    public String emne;
    public String innhold;
    public List<PEpostVedlegg> vedlegg = new ArrayList<>();
}

package no.nav.syfo.repository.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class PEpostVedlegg {
    public Long id;
    public long epostId;
    public String innhold;
    public String type;
}

package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class EpostVedlegg {

    public enum VedleggType {
        ICS
    }

    public String innhold;
    public long epostId;
    public VedleggType type;
}

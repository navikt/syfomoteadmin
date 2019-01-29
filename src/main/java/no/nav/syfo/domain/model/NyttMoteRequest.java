package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
public class NyttMoteRequest {
    public List<TidOgSted> alternativer = new ArrayList<>();
    public String fnr;
    public String orgnummer;
    public String navn;
    public String epost;
    public String navEnhet;
}

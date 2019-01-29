package no.nav.syfo.api.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
public class RSOverforMoter {
    public List<String> moteUuidListe = new ArrayList<>();
}

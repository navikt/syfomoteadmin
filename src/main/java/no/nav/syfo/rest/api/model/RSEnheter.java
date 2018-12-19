package no.nav.syfo.rest.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class RSEnheter {

    public List<RSEnhet> enhetliste = new ArrayList<>();

}

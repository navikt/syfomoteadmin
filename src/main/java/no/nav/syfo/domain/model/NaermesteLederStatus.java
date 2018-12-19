package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class NaermesteLederStatus {
    public boolean erAktiv;
    public LocalDate aktivFom;
    public LocalDate aktivTom;
}

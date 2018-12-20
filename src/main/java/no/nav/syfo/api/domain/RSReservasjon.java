package no.nav.syfo.api.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class RSReservasjon {


    public enum KontaktInfoFeilAarsak {
        RESERVERT,
        KODE6,
        INGEN_KONTAKTINFORMASJON,
        UTGAATT
    }

    public Boolean skalHaVarsel;
    public KontaktInfoFeilAarsak feilAarsak;
}

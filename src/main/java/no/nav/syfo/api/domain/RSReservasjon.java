package no.nav.syfo.api.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
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


package no.nav.syfo.api.mappers;

import no.nav.syfo.api.domain.RSEnhet;
import no.nav.syfo.domain.model.Enhet;

import java.util.function.Function;

public class RSEnhetMapper {

    public static Function<Enhet, RSEnhet> enhet2rs = enhet -> new RSEnhet()
            .enhetId(enhet.enhetId)
            .navn(enhet.navn);
}

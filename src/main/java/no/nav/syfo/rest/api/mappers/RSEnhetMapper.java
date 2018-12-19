package no.nav.syfo.rest.api.mappers;

import no.nav.syfo.rest.api.model.RSEnhet;
import no.nav.syfo.domain.model.Enhet;

import java.util.function.Function;

public class RSEnhetMapper {

    public static Function<Enhet, RSEnhet> enhet2rs = enhet -> new RSEnhet()
            .enhetId(enhet.enhetId)
            .navn(enhet.navn);
}

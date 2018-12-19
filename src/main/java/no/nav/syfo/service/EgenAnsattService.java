package no.nav.syfo.service;

import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest;
import org.springframework.cache.annotation.Cacheable;

import javax.inject.Inject;

public class EgenAnsattService {

    @Inject
    private EgenAnsattV1 egenAnsattV1;

    @Cacheable(value = "egenansatt", keyGenerator = "userkeygenerator")
    public boolean erEgenAnsatt(String fnr) {
        return egenAnsattV1.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(new WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest()
                .withIdent(fnr)
        ).isEgenAnsatt();
    }
}

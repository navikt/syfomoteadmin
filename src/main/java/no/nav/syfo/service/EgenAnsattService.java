package no.nav.syfo.service;

import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static no.nav.syfo.config.CacheConfig.CACHENAME_EGENANSATT;

@Service
public class EgenAnsattService {

    private EgenAnsattV1 egenAnsattV1;

    @Autowired
    public EgenAnsattService(
            EgenAnsattV1 egenAnsattV1
    ) {
        this.egenAnsattV1 = egenAnsattV1;
    }

    @Cacheable(value = CACHENAME_EGENANSATT, key = "#fnr", condition = "#fnr != null")
    public boolean erEgenAnsatt(String fnr) {
        return egenAnsattV1.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(new WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest()
                .withIdent(fnr)
        ).isEgenAnsatt();
    }
}

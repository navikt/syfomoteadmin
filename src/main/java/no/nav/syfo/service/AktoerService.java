package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.service.exceptions.MoteException;
import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2;
import no.nav.tjeneste.virksomhet.aktoer.v2.HentAktoerIdForIdentPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.HentIdentForAktoerIdPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentAktoerIdForIdentRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentIdentForAktoerIdRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static no.nav.syfo.config.CacheConfig.CACHENAME_AKTOR_FNR;
import static no.nav.syfo.config.CacheConfig.CACHENAME_AKTOR_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Service
public class AktoerService {

    private AktoerV2 aktoerV2;

    @Autowired
    public AktoerService(
            AktoerV2 aktoerV2
    ) {
        this.aktoerV2 = aktoerV2;
    }

    @Cacheable(value = CACHENAME_AKTOR_ID, key = "#fnr", condition = "#fnr != null")
    public String hentAktoerIdForIdent(String fnr) {
        if (isBlank(fnr) || !fnr.matches("\\d{11}$")) {
            log.error("Forsøker å hente aktørId for fnr {} på feil format", fnr);
            throw new RuntimeException();
        }

        try {
            return aktoerV2.hentAktoerIdForIdent(
                    new WSHentAktoerIdForIdentRequest()
                            .withIdent(fnr)
            ).getAktoerId();
        } catch (HentAktoerIdForIdentPersonIkkeFunnet e) {
            throw new MoteException("AktoerID ikke funnet for fødselsnummer!");
        }
    }

    @Cacheable(value = CACHENAME_AKTOR_FNR, key = "#aktoerId", condition = "#aktoerId != null")
    public String hentFnrForAktoer(String aktoerId) {
        if (isBlank(aktoerId) || !aktoerId.matches("\\d{13}$")) {
            log.error("Forsøker å hente fnr for aktørId {} på feil format", aktoerId);
            throw new RuntimeException();
        }

        try {
            return aktoerV2.hentIdentForAktoerId(
                    new WSHentIdentForAktoerIdRequest()
                            .withAktoerId(aktoerId)
            ).getIdent();
        } catch (HentIdentForAktoerIdPersonIkkeFunnet e) {
            throw new MoteException("FNR ikke funnet for aktoerId!");
        }
    }
}

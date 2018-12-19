package no.nav.syfo.service;

import no.nav.syfo.service.exceptions.MoteException;
import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2;
import no.nav.tjeneste.virksomhet.aktoer.v2.HentAktoerIdForIdentPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.HentIdentForAktoerIdPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentAktoerIdForIdentRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentIdentForAktoerIdRequest;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;

import javax.inject.Inject;

import static no.nav.syfo.util.SubjectHandlerUtil.getUserId;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

public class AktoerService {

    private static final Logger LOG = getLogger(AktoerService.class);

    @Inject
    private AktoerV2 aktoerV2;

    @Cacheable(value = "aktoer")
    public String hentAktoerIdForIdent(String fnr) {
        if (isBlank(fnr) || !fnr.matches("\\d{11}$")) {
            LOG.error("{} forsøker å hente fnr {}", getUserId(), fnr);
            throw new RuntimeException();
        }

        try {
            return aktoerV2.hentAktoerIdForIdent(
                    new WSHentAktoerIdForIdentRequest()
                            .withIdent(fnr)
            ).getAktoerId();
        } catch (HentAktoerIdForIdentPersonIkkeFunnet e) {
            LOG.warn("AktoerID ikke funnet for fødselsnummer!", e);
            throw new MoteException("AktoerID ikke funnet for fødselsnummer!");
        }
    }

    @Cacheable(value = "aktoer")
    public String hentFnrForAktoer(String aktoerId) {
        if (isBlank(aktoerId) || !aktoerId.matches("\\d{13}$")) {
            LOG.error("{} forsøker å hente fnr {}", getUserId(), aktoerId);
            throw new RuntimeException();
        }

        try {
            return aktoerV2.hentIdentForAktoerId(
                    new WSHentIdentForAktoerIdRequest()
                            .withAktoerId(aktoerId)
            ).getIdent();
        } catch (HentIdentForAktoerIdPersonIkkeFunnet e) {
            LOG.warn("FNR ikke funnet for aktoerId!", e);
            throw new MoteException("FNR ikke funnet for aktoerId!");
        }
    }
}

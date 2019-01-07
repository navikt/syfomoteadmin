package no.nav.syfo.service;

import no.nav.dialogarena.aktor.AktorService;
import no.nav.syfo.service.exceptions.MoteException;
import org.slf4j.Logger;

import javax.inject.Inject;

import static no.nav.syfo.util.SubjectHandlerUtil.getUserId;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

public class AktoerService {

    private static final Logger LOG = getLogger(AktoerService.class);

    @Inject
    AktorService aktorService;

    public String hentAktoerIdForIdent(String fnr) {
        if (isBlank(fnr) || !fnr.matches("\\d{11}$")) {
            LOG.error("{} forsøker å hente fnr {}", getUserId(), fnr);
            throw new RuntimeException();
        }

        return aktorService.getAktorId(fnr).orElseThrow(() -> new MoteException("AktoerID ikke funnet for fødselsnummer!"));
    }

    public String hentFnrForAktoer(String aktoerId) {
        if (isBlank(aktoerId) || !aktoerId.matches("\\d{13}$")) {
            LOG.error("{} forsøker å hente fnr {}", getUserId(), aktoerId);
            throw new RuntimeException();
        }

        return aktorService.getFnr(aktoerId).orElseThrow(() -> new MoteException("FNR ikke funnet for aktoerId!"));

    }
}

package no.nav.syfo.service;

import no.nav.syfo.domain.model.Ansatt;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import static no.nav.syfo.util.SubjectHandlerUtil.getUserId;
import static org.slf4j.LoggerFactory.getLogger;

public class BrukertilgangService {

    private static final Logger LOG = getLogger(BrukertilgangService.class);

    @Inject
    private AktoerService aktoerService;
    @Inject
    private BrukerprofilService brukerprofilService;
    @Inject
    private SykefravaersoppfoelgingService sykefravaersoppfoelgingService;

    public void kastExceptionHvisIkkeTilgang(String oppslattBrukerIdent) {
        String innloggetIdent = getUserId();
        boolean harTilgang = harTilgangTilOppslaattBruker(innloggetIdent, oppslattBrukerIdent);
        if (!harTilgang) {
            LOG.error("Ikke tilgang");
            throw new ForbiddenException();
        }
    }

    boolean harTilgangTilOppslaattBruker(String innloggetIdent, String brukerFnr) {
        try {
            return !(sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(innloggetIdent, brukerFnr)
                    || brukerprofilService.hentBruker(brukerFnr).erKode6);
        } catch (ForbiddenException e) {
            return false;
        }
    }

    boolean sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(String innloggetIdent, String oppslaattFnr) {
        return !(sporInnloggetBrukerOmSegSelv(innloggetIdent, oppslaattFnr) || sporInnloggetBrukerOmEnAnsatt(innloggetIdent, oppslaattFnr));
    }

    private boolean sporInnloggetBrukerOmEnAnsatt(String innloggetIdent, String oppslaattFnr) {
        String innloggetAktoerId = aktoerService.hentAktoerIdForIdent(innloggetIdent);
        String oppslaattAktoerId = aktoerService.hentAktoerIdForIdent(oppslaattFnr);
        return sykefravaersoppfoelgingService.hentNaermesteLedersAnsattListe(innloggetAktoerId)
                .stream()
                .map(Ansatt::aktoerId)
                .anyMatch(oppslaattAktoerId::equals);
    }

    private boolean sporInnloggetBrukerOmSegSelv(String innloggetIdent, String brukerFnr) {
        return brukerFnr.equals(innloggetIdent);
    }
}

package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.domain.model.Ansatt;
import no.nav.syfo.oidc.OIDCIssuer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.ForbiddenException;

import static no.nav.syfo.util.OIDCUtil.getSubjectEkstern;

@Slf4j
@Service
public class BrukertilgangService {

    private OIDCRequestContextHolder contextHolder;

    private AktoerService aktoerService;

    private PersonService personService;

    private SykefravaersoppfoelgingService sykefravaersoppfoelgingService;

    @Autowired
    public BrukertilgangService(
            OIDCRequestContextHolder contextHolder,
            AktoerService aktoerService,
            PersonService personService,
            SykefravaersoppfoelgingService sykefravaersoppfoelgingService
    ) {
        this.contextHolder = contextHolder;
        this.aktoerService = aktoerService;
        this.personService = personService;
        this.sykefravaersoppfoelgingService = sykefravaersoppfoelgingService;
    }

    public void kastExceptionHvisIkkeTilgang(String oppslattBrukerIdent) {
        String innloggetIdent = getSubjectEkstern(contextHolder);
        boolean harTilgang = harTilgangTilOppslaattBruker(innloggetIdent, oppslattBrukerIdent);
        if (!harTilgang) {
            log.error("Ikke tilgang");
            throw new ForbiddenException();
        }
    }

    boolean harTilgangTilOppslaattBruker(String innloggetIdent, String brukerFnr) {
        try {
            return !(sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(innloggetIdent, brukerFnr)
                    || personService.erPersonKode6(brukerFnr));
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
        return sykefravaersoppfoelgingService.hentNaermesteLedersAnsattListe(innloggetAktoerId, OIDCIssuer.EKSTERN)
                .stream()
                .map(Ansatt::aktoerId)
                .anyMatch(oppslaattAktoerId::equals);
    }

    private boolean sporInnloggetBrukerOmSegSelv(String innloggetIdent, String brukerFnr) {
        return brukerFnr.equals(innloggetIdent);
    }
}

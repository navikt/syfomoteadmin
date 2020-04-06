package no.nav.syfo.service;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.brukertilgang.BrukertilgangConsumer;
import no.nav.syfo.pdl.PdlConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.ws.rs.ForbiddenException;

import static no.nav.syfo.config.CacheConfig.CACHENAME_TILGANG_IDENT;
import static no.nav.syfo.util.OIDCUtil.getSubjectEkstern;

@Service
public class BrukertilgangService {

    private static final Logger LOG = LoggerFactory.getLogger(BrukertilgangService.class);

    private final OIDCRequestContextHolder contextHolder;
    private final BrukertilgangConsumer brukertilgangConsumer;
    private final PdlConsumer pdlConsumer;

    @Autowired
    public BrukertilgangService(
            OIDCRequestContextHolder contextHolder,
            BrukertilgangConsumer brukertilgangConsumer,
            PdlConsumer pdlConsumer
    ) {
        this.contextHolder = contextHolder;
        this.brukertilgangConsumer = brukertilgangConsumer;
        this.pdlConsumer = pdlConsumer;
    }

    public void kastExceptionHvisIkkeTilgang(String oppslattBrukerIdent) {
        String innloggetIdent = getSubjectEkstern(contextHolder);
        boolean harTilgang = harTilgangTilOppslaattBruker(innloggetIdent, oppslattBrukerIdent);
        if (!harTilgang) {
            LOG.warn("Innlogget Ident har ikke tilgang til Oppslatt Ident");
            throw new ForbiddenException();
        }
    }

    boolean harTilgangTilOppslaattBruker(String innloggetIdent, String brukerFnr) {
        try {
            return !(sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(innloggetIdent, brukerFnr)
                    || pdlConsumer.isKode6(brukerFnr));
        } catch (ForbiddenException e) {
            return false;
        }
    }

    @Cacheable(cacheNames = CACHENAME_TILGANG_IDENT, key = "#innloggetIdent.concat(#oppslaattFnr)", condition = "#innloggetIdent != null && #oppslaattFnr != null")
    public boolean sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(String innloggetIdent, String oppslaattFnr) {
        return !(oppslaattFnr.equals(innloggetIdent) || brukertilgangConsumer.hasAccessToAnsatt(oppslaattFnr));
    }
}

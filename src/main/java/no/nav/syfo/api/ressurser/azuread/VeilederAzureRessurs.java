package no.nav.syfo.api.ressurser.azuread;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.api.domain.RSEnheter;
import no.nav.syfo.api.domain.RSVeilederInfo;
import no.nav.syfo.service.NorgService;
import no.nav.syfo.service.VeilederService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.api.mappers.RSEnhetMapper.enhet2rs;
import static no.nav.syfo.oidc.OIDCIssuer.AZURE;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.OIDCUtil.getSubjectInternAzure;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/internad/veilederinfo")
@ProtectedWithClaims(issuer = AZURE)
public class VeilederAzureRessurs {

    private OIDCRequestContextHolder contextHolder;
    private NorgService norgService;
    private VeilederService veilederService;

    @Inject
    public VeilederAzureRessurs(
            OIDCRequestContextHolder contextHolder,
            NorgService norgService,
            VeilederService veilederService
    ) {
        this.contextHolder = contextHolder;
        this.norgService = norgService;
        this.veilederService = veilederService;
    }

    @GetMapping
    public RSVeilederInfo hentNavn() {
        return hentIdent(getSubjectInternAzure(contextHolder));
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/{ident}")
    public RSVeilederInfo hentIdent(@PathVariable("ident") String ident) {
        return new RSVeilederInfo()
                .navn(veilederService.hentVeileder(ident).navn)
                .ident(ident);
    }

    @GetMapping(value = "/enheter", produces = APPLICATION_JSON_VALUE)
    public RSEnheter hentEnheter() {
        return new RSEnheter()
                .enhetliste(mapListe(norgService.hentVeiledersNavEnheter(getSubjectInternAzure(contextHolder)), enhet2rs));
    }
}

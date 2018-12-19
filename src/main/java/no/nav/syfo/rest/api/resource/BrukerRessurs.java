package no.nav.syfo.rest.api.resource;

import no.nav.syfo.domain.model.Kontaktinfo;
import no.nav.syfo.rest.api.model.RSBruker;
import no.nav.syfo.rest.api.model.RSReservasjon;
import no.nav.syfo.rest.services.TilgangService;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.BrukerprofilService;
import no.nav.syfo.service.DkifService;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.syfo.domain.model.Kontaktinfo.FeilAarsak.*;

@Controller
@Path("/brukerinfo/{ident}")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class BrukerRessurs {

    @Inject
    private BrukerprofilService brukerprofilService;
    @Inject
    private DkifService dkifService;
    @Inject
    private AktoerService aktoerService;
    @Inject
    private TilgangService tilgangService;

    @GET
    @Path("/navn")
    public RSBruker hentBruker(@PathParam("ident") String ident) {
        String fnr;
        if (ident.matches("\\d{11}$")) {
            fnr = ident;
        } else {
            fnr = aktoerService.hentFnrForAktoer(ident);
        }

        if (tilgangService.sjekkTilgangTilPerson(fnr).getStatus() == 200) {
            return new RSBruker().navn(brukerprofilService.hentBruker(fnr).navn);
        } else {
            throw new ForbiddenException("Innlogget bruker har ikke tilgang til denne personen");
        }
    }

    @GET
    public RSBruker bruker(@PathParam("ident") String ident) {
        String fnr;
        if (ident.matches("\\d{11}$")) {
            fnr = ident;
        } else {
            fnr = aktoerService.hentFnrForAktoer(ident);
        }
        if (tilgangService.sjekkTilgangTilPerson(fnr).getStatus() == 200) {
            RSBruker rsBruker = new RSBruker();
            Kontaktinfo kontaktinfo = dkifService.hentKontaktinfoFnr(fnr);
            rsBruker.kontaktinfo
                    .tlf(kontaktinfo.tlf)
                    .epost(kontaktinfo.epost)
                    .reservasjon(rsBruker.kontaktinfo.reservasjon
                            .skalHaVarsel(kontaktinfo.skalHaVarsel)
                            .feilAarsak(!kontaktinfo.skalHaVarsel ? feilAarsak(kontaktinfo.feilAarsak) : null));
            return rsBruker
                    .navn(brukerprofilService.hentBruker(fnr).navn);
        } else {
            throw new ForbiddenException("Innlogget bruker har ikke tilgang til denne personen");
        }
    }

    private RSReservasjon.KontaktInfoFeilAarsak feilAarsak(Kontaktinfo.FeilAarsak feilAarsak) {
        if (feilAarsak == SIKKERHETSBEGRENSNING) {
            return RSReservasjon.KontaktInfoFeilAarsak.KODE6;
        } else if (feilAarsak == KONTAKTINFO_IKKE_FUNNET || feilAarsak == PERSON_IKKE_FUNNET) {
            return RSReservasjon.KontaktInfoFeilAarsak.INGEN_KONTAKTINFORMASJON;
        } else if (feilAarsak == Kontaktinfo.FeilAarsak.RESERVERT) {
            return RSReservasjon.KontaktInfoFeilAarsak.RESERVERT;
        } else if (feilAarsak == Kontaktinfo.FeilAarsak.UTGAATT) {
            return RSReservasjon.KontaktInfoFeilAarsak.UTGAATT;
        } else {
            throw new RuntimeException("Fant ikke feilårsak. Sjekk mappingen");
        }
    }


}

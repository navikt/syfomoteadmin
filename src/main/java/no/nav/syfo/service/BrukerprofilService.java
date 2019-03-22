package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.model.TpsPerson;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.*;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.informasjon.WSNorskIdent;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.informasjon.WSPerson;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.meldinger.WSHentKontaktinformasjonOgPreferanserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static no.nav.syfo.config.CacheConfig.CACHENAME_TPS_BRUKER;
import static no.nav.syfo.config.CacheConfig.CACHENAME_TPS_NAVN;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.text.WordUtils.capitalize;

@Slf4j
@Service
public class BrukerprofilService {

    private BrukerprofilV3 brukerprofilV3;

    private AktoerService aktoerService;

    @Autowired
    public BrukerprofilService(
            BrukerprofilV3 brukerprofilV3,
            AktoerService aktoerService
    ) {
        this.brukerprofilV3 = brukerprofilV3;
        this.aktoerService = aktoerService;
    }

    @Cacheable(value = CACHENAME_TPS_NAVN, key = "#aktoerId", condition = "#aktoerId != null")
    public String finnBrukerPersonnavnByAktoerId(String aktoerId) {
        return finnBrukerPersonnavnByFnr(aktoerService.hentFnrForAktoer(aktoerId));
    }

    private String finnBrukerPersonnavnByFnr(String fnr) {
        return hentBruker(fnr).navn;
    }

    @Cacheable(value = CACHENAME_TPS_BRUKER, key = "#fnr", condition = "#fnr != null")
    public TpsPerson hentBruker(String fnr) {
        if (isBlank(fnr) || !fnr.matches("\\d{11}$")) {
            log.error("Forsøker å hente brukerinfo for fnr {}", fnr);
            throw new RuntimeException();
        }
        try {
            WSPerson wsPerson = brukerprofilV3.hentKontaktinformasjonOgPreferanser(new WSHentKontaktinformasjonOgPreferanserRequest()
                    .withIdent(new WSNorskIdent()
                            .withIdent(fnr))).getBruker();

            if (wsPerson.getDiskresjonskode() != null && ("6".equals(wsPerson.getDiskresjonskode().getValue()) || "7".equals(wsPerson.getDiskresjonskode().getValue()))) {
                TpsPerson tpsPerson = new TpsPerson();
                if ("6".equals(wsPerson.getDiskresjonskode().getValue())) {
                    tpsPerson.erKode6(true);
                }
                return tpsPerson.navn("skjermet bruker").skjermetBruker(true);
            }

            String mellomnavn = wsPerson.getPersonnavn().getMellomnavn() == null ? "" : wsPerson.getPersonnavn().getMellomnavn();
            if (!"".equals(mellomnavn)) {
                mellomnavn = mellomnavn + " ";
            }
            String navnFraTps = wsPerson.getPersonnavn().getFornavn() + " " + mellomnavn + wsPerson.getPersonnavn().getEtternavn();
            return new TpsPerson().navn(capitalize(navnFraTps.toLowerCase(), '-', ' '));
        } catch (HentKontaktinformasjonOgPreferanserPersonIdentErUtgaatt | HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning | HentKontaktinformasjonOgPreferanserPersonIkkeFunnet e) {
            log.warn("Exception mot TPS med ident {}", fnr, e);
            return new TpsPerson().navn("Vi fant ikke navnet");
        } catch (RuntimeException e) {
            log.error("Runtimefeil mot TPS. Kaster videre istedenfor å håndtere fordi bruker kan være kode 6.", e);
            throw e;
        }
    }
}

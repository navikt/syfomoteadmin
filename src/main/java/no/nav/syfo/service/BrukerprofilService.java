package no.nav.syfo.service;

import no.nav.syfo.domain.model.TpsPerson;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.BrukerprofilV3;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.HentKontaktinformasjonOgPreferanserPersonIdentErUtgaatt;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.informasjon.WSNorskIdent;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.informasjon.WSPerson;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.meldinger.WSHentKontaktinformasjonOgPreferanserRequest;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;

import javax.inject.Inject;

import static no.nav.syfo.util.SubjectHandlerUtil.getUserId;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.text.WordUtils.capitalize;
import static org.slf4j.LoggerFactory.getLogger;

public class BrukerprofilService {
    private static final Logger LOG = getLogger(BrukerprofilService.class);

    @Inject
    private BrukerprofilV3 brukerprofilV3;
    @Inject
    private AktoerService aktoerService;

    @Cacheable(value = "tpsnavn", keyGenerator = "userkeygenerator")
    public String finnBrukerPersonnavnByAktoerId(String aktoerId) {
        return finnBrukerPersonnavnByFnr(aktoerService.hentFnrForAktoer(aktoerId));
    }

    private String finnBrukerPersonnavnByFnr(String fnr) {
        return hentBruker(fnr).navn;
    }

    @Cacheable(value = "tpsbruker", keyGenerator = "userkeygenerator")
    public TpsPerson hentBruker(String fnr) {
        if (isBlank(fnr) || !fnr.matches("\\d{11}$")) {
            LOG.error("{} forsøker å hente fnr {}", getUserId(), fnr);
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
            LOG.warn("Exception mot TPS med ident {}", fnr, e);
            return new TpsPerson().navn("Vi fant ikke navnet");
        } catch (RuntimeException e) {
            LOG.error("Runtimefeil mot TPS. Kaster videre istedenfor å håndtere fordi bruker kan være kode 6.", e);
            throw e;
        }
    }


}

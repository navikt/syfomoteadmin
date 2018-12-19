package no.nav.syfo.service;

import no.nav.syfo.domain.model.Enhet;
import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.*;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class NorgService {
    private static final Logger LOG = getLogger(NorgService.class);

    @Inject
    private OrganisasjonRessursEnhetV1 organisasjonRessursEnhetV1;

    @Cacheable(value = "norg")
    public List<Enhet> hentVeiledersNavEnheter(String veiledersIdent) {
        try {
            return organisasjonRessursEnhetV1.hentEnhetListe(new WSHentEnhetListeRequest().withRessursId(veiledersIdent))
                    .getEnhetListe()
                    .stream()
                    .map(ws2enhet)
                    .collect(toList());
        } catch (HentEnhetListeUgyldigInput | HentEnhetListeRessursIkkeFunnet e) {
            LOG.error("Feil ved henting av enheter fra Norg for ident {}", veiledersIdent, e);
            throw new RuntimeException();
        } catch (RuntimeException e) {
            LOG.error("RuntimeException ved henting av enheter fra Norg for ident {}", veiledersIdent, e);
            throw e;
        }
    }

    public boolean hoererNavEnhetTilBruker(String navEnhet, String veilederIdent) {
        return hentVeiledersNavEnheter(veilederIdent).stream()
                .anyMatch(enhet -> enhet.enhetId.equals(navEnhet));
    }

    private static Function<WSEnhet, Enhet> ws2enhet = wsEnhet -> new Enhet()
            .enhetId(wsEnhet.getEnhetId())
            .navn(wsEnhet.getNavn());

}

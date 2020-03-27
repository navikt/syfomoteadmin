package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.model.Enhet;
import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static no.nav.syfo.config.CacheConfig.CACHENAME_NORG_ENHETER;

@Slf4j
@Service
public class NorgService {

    private OrganisasjonRessursEnhetV1 organisasjonRessursEnhetV1;

    @Inject
    public NorgService(OrganisasjonRessursEnhetV1 organisasjonRessursEnhetV1) {
        this.organisasjonRessursEnhetV1 = organisasjonRessursEnhetV1;
    }

    @Cacheable(value = CACHENAME_NORG_ENHETER, key = "#veiledersIdent", condition = "#veiledersIdent != null")
    public List<Enhet> hentVeiledersNavEnheter(String veiledersIdent) {
        try {
            return organisasjonRessursEnhetV1.hentEnhetListe(new WSHentEnhetListeRequest().withRessursId(veiledersIdent))
                    .getEnhetListe()
                    .stream()
                    .map(ws2enhet)
                    .collect(toList());
        } catch (HentEnhetListeUgyldigInput | HentEnhetListeRessursIkkeFunnet e) {
            log.error("Feil ved henting av enheter fra Norg for ident {}", veiledersIdent, e);
            throw new RuntimeException();
        } catch (RuntimeException e) {
            log.error("RuntimeException ved henting av enheter fra Norg for ident {}", veiledersIdent, e);
            throw e;
        }
    }

    private static Function<WSEnhet, Enhet> ws2enhet = wsEnhet -> new Enhet()
            .enhetId(wsEnhet.getEnhetId())
            .navn(wsEnhet.getNavn());

}

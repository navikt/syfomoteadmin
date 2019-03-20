package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.organisasjon.v4.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.WSUstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.WSHentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.WSHentOrganisasjonResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static java.util.stream.Collectors.joining;
import static no.nav.syfo.config.CacheConfig.CACHENAME_EREG_NAVN;

@Slf4j
@Service
public class OrganisasjonService {

    private OrganisasjonV4 organisasjonV4;

    @Inject
    public OrganisasjonService(OrganisasjonV4 organisasjonV4) {
        this.organisasjonV4 = organisasjonV4;
    }

    @Cacheable(value = CACHENAME_EREG_NAVN, key = "#orgnr", condition = "#orgnr != null")
    public String hentNavn(String orgnr) {
        try {
            WSHentOrganisasjonResponse response = organisasjonV4.hentOrganisasjon(request(orgnr));
            WSUstrukturertNavn ustrukturertNavn = (WSUstrukturertNavn) response.getOrganisasjon().getNavn();

            return ustrukturertNavn.getNavnelinje().stream()
                    .filter(StringUtils::isNotBlank)
                    .collect(joining(", "));

        } catch (HentOrganisasjonOrganisasjonIkkeFunnet e) {
            log.warn("Kunne ikke hente organisasjon for {}", orgnr, e);
        } catch (HentOrganisasjonUgyldigInput e) {
            log.warn("Ugyldig input for {}", orgnr, e);
        } catch (RuntimeException e) {
            log.error("Feil ved henting av Organisasjon {}", orgnr, e);
        }
        return "Fant ikke navn";
    }

    private WSHentOrganisasjonRequest request(String orgnr) {
        return new WSHentOrganisasjonRequest()
                .withOrgnummer(orgnr)
                .withInkluderHierarki(false)
                .withInkluderHistorikk(false);
    }
}

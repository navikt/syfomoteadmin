package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.model.Enhet;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.HentOverordnetEnhetListeEnhetIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSEnhetRelasjonstyper;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.WSHentOverordnetEnhetListeRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Optional;

import static no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSEnhetsstatus.AKTIV;

@Component
@Slf4j
public class OrganisasjonEnhetService {

    private final OrganisasjonEnhetV2 organisasjonEnhetV2;

    @Inject
    public OrganisasjonEnhetService(OrganisasjonEnhetV2 organisasjonEnhetV2) {
        this.organisasjonEnhetV2 = organisasjonEnhetV2;
    }

    public Optional<Enhet> finnSetteKontor(String enhet) {
        try {
            return organisasjonEnhetV2.hentOverordnetEnhetListe(new WSHentOverordnetEnhetListeRequest()
                    .withEnhetId(enhet).withEnhetRelasjonstype(new WSEnhetRelasjonstyper().withValue("HABILITET")))
                    .getOverordnetEnhetListe()
                    .stream()
                    .filter(wsOrganisasjonsenhet -> AKTIV.equals(wsOrganisasjonsenhet.getStatus()))
                    .map(wsOrganisasjonsenhet -> new Enhet().enhetId(wsOrganisasjonsenhet.getEnhetId()).navn(wsOrganisasjonsenhet.getEnhetNavn()))
                    .findFirst();
        } catch (HentOverordnetEnhetListeEnhetIkkeFunnet e) {
            log.error("Fant ingen overordnet enhet for enhet {}", enhet);
            throw new RuntimeException();
        }
    }

}

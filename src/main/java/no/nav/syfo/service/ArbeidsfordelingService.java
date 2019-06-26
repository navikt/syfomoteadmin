package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.model.Enhet;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.FinnBehandlendeEnhetListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnBehandlendeEnhetListeRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import static no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.WSEnhetsstatus.AKTIV;

@Component
@Slf4j
public class ArbeidsfordelingService {

    private final ArbeidsfordelingV1 arbeidsfordelingV1;

    public ArbeidsfordelingService(ArbeidsfordelingV1 arbeidsfordelingV1) {
        this.arbeidsfordelingV1 = arbeidsfordelingV1;
    }

    public Enhet finnAktivBehandlendeEnhet(String geografiskTilknytning) {
        try {
            return arbeidsfordelingV1.finnBehandlendeEnhetListe(
                    new WSFinnBehandlendeEnhetListeRequest()
                            .withArbeidsfordelingKriterier(new WSArbeidsfordelingKriterier()
                                    .withGeografiskTilknytning(new WSGeografi().withValue(geografiskTilknytning))
                                    .withTema(new WSTema().withValue("OPP")))
            ).getBehandlendeEnhetListe()
                    .stream()
                    .filter(wsOrganisasjonsenhet -> AKTIV.equals(wsOrganisasjonsenhet.getStatus()))
                    .map(wsOrganisasjonsenhet -> new Enhet().enhetId(wsOrganisasjonsenhet.getEnhetId()).navn(wsOrganisasjonsenhet.getEnhetNavn()))
                    .findFirst()
                    .orElse(new Enhet()
                            .enhetId(geografiskTilknytning)
                            .navn(geografiskTilknytning)
                    );
        } catch (FinnBehandlendeEnhetListeUgyldigInput e) {
            log.error("Feil ved henting av brukers forvaltningsenhet med geografiskTilknytning: " + geografiskTilknytning);
            throw new RuntimeException("Feil ved henting av brukers forvaltningsenhet", e);
        } catch (RuntimeException e) {
            log.error("Feil ved henting av behandlende enhet for geografiskTilknytning: " + geografiskTilknytning);
            throw e;
        }
    }

}

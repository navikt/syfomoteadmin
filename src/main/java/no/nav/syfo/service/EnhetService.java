package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.model.Enhet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EnhetService {

    private final ArbeidsfordelingService arbeidsfordelingService;

    private final EgenAnsattService egenAnsattService;

    private final OrganisasjonEnhetService organisasjonEnhetService;

    private final PersonService personService;

    @Autowired
    public EnhetService(
            ArbeidsfordelingService arbeidsfordelingService,
            EgenAnsattService egenAnsattService,
            OrganisasjonEnhetService organisasjonEnhetService,
            PersonService personService
    ) {
        this.arbeidsfordelingService = arbeidsfordelingService;
        this.egenAnsattService = egenAnsattService;
        this.organisasjonEnhetService = organisasjonEnhetService;
        this.personService = personService;
    }

    public String finnArbeidstakersBehandlendeEnhet(String arbeidstakerFnr) {
        String geografiskTilknytning = personService.hentGeografiskTilknytning(arbeidstakerFnr);
        Enhet enhet = arbeidsfordelingService.finnAktivBehandlendeEnhet(geografiskTilknytning);
        if (egenAnsattService.erEgenAnsatt(arbeidstakerFnr)) {
            Enhet overordnetEnhet = organisasjonEnhetService.finnSetteKontor(enhet.enhetId()).orElse(enhet);
            return overordnetEnhet.enhetId();
        }
        return enhet.enhetId();
    }
}

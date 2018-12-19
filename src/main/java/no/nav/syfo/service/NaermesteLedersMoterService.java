package no.nav.syfo.service;

import no.nav.syfo.domain.model.Ansatt;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.TidOgSted;
import org.springframework.cache.annotation.Cacheable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class NaermesteLedersMoterService {
    @Inject
    private SykefravaersoppfoelgingService sykefravaersoppfoelgingService;
    @Inject
    private MoteService moteService;

    @Cacheable(value = "syfo", keyGenerator = "userkeygenerator")
    public List<Mote> hentNaermeteLedersMoter(String nlAktoerId) {
        List<Ansatt> ansatte = sykefravaersoppfoelgingService.hentNaermesteLedersAnsattListe(nlAktoerId);

        List<Mote> moter = new ArrayList<>();
        for (Ansatt ansatt : ansatte) {
            List<Mote> ansattesMoter = moteService.findMoterByBrukerAktoerIdOgAGOrgnummer(ansatt.aktoerId, ansatt.orgnummer);

            ansattesMoter.removeIf(mote -> mote
                    .alternativer
                    .stream()
                    .allMatch(aktivFomErEtterAlternativtidspunkt(ansatt)));

            moter.addAll(ansattesMoter);
        }
        return moter;
    }

    private Predicate<TidOgSted> aktivFomErEtterAlternativtidspunkt(Ansatt ansatt) {
        return tidOgSted -> ansatt
                .naermesteLederStatus
                .aktivFom
                .isAfter(tidOgSted.tid.toLocalDate());
    }
}

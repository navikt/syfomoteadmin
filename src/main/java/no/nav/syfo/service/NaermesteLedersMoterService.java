package no.nav.syfo.service;

import no.nav.syfo.domain.model.Ansatt;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.TidOgSted;
import no.nav.syfo.oidc.OIDCIssuer;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Service
public class NaermesteLedersMoterService {

    private SykefravaersoppfoelgingService sykefravaersoppfoelgingService;

    private MoteService moteService;

    @Inject
    public NaermesteLedersMoterService(
            SykefravaersoppfoelgingService sykefravaersoppfoelgingService,
            MoteService moteService
    ) {
        this.sykefravaersoppfoelgingService = sykefravaersoppfoelgingService;
        this.moteService = moteService;
    }

    public List<Mote> hentNaermesteLedersMoter(String nlAktoerId) {
        List<Ansatt> ansatte = sykefravaersoppfoelgingService.hentNaermesteLedersAnsattListe(nlAktoerId, OIDCIssuer.EKSTERN);

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

package no.nav.syfo.service;

import no.nav.syfo.domain.model.*;
import no.nav.syfo.consumer.narmesteleder.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Predicate;

@Service
public class NaermesteLedersMoterService {

    private NarmesteLederConsumer narmesteLederConsumer;

    private MoteService moteService;

    @Inject
    public NaermesteLedersMoterService(
            NarmesteLederConsumer narmesteLederConsumer,
            MoteService moteService
    ) {
        this.narmesteLederConsumer = narmesteLederConsumer;
        this.moteService = moteService;
    }

    public List<Mote> hentNaermesteLedersMoter(String nlAktoerId) {
        List<NarmesteLederRelasjon> narmesteLederRelasjoner = narmesteLederConsumer.narmestelederRelasjonerAnsatte(nlAktoerId);

        List<Mote> moter = new ArrayList<>();
        for (NarmesteLederRelasjon narmesteLederRelasjon : narmesteLederRelasjoner) {
            List<Mote> ansattesMoter = moteService.findMoterByBrukerAktoerIdOgAGOrgnummer(narmesteLederRelasjon.getAktorId(), narmesteLederRelasjon.getOrgnummer());

            ansattesMoter.removeIf(mote -> mote
                    .alternativer
                    .stream()
                    .allMatch(aktivFomErEtterAlternativtidspunkt(narmesteLederRelasjon)));

            moter.addAll(ansattesMoter);
        }
        return moter;
    }

    private Predicate<TidOgSted> aktivFomErEtterAlternativtidspunkt(NarmesteLederRelasjon ansatt) {
        return tidOgSted -> ansatt
                .getAktivFom()
                .isAfter(tidOgSted.tid.toLocalDate());
    }
}

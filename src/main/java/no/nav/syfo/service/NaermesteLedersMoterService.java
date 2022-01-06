package no.nav.syfo.service;

import no.nav.syfo.consumer.narmesteleder.NarmesteLederConsumer;
import no.nav.syfo.consumer.narmesteleder.NarmesteLederRelasjonDTO;
import no.nav.syfo.consumer.pdl.PdlConsumer;
import no.nav.syfo.domain.AktorId;
import no.nav.syfo.domain.Fodselsnummer;
import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.TidOgSted;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Service
public class NaermesteLedersMoterService {

    private NarmesteLederConsumer narmesteLederConsumer;

    private MoteService moteService;

    private PdlConsumer pdlConsumer;

    @Inject
    public NaermesteLedersMoterService(
            NarmesteLederConsumer narmesteLederConsumer,
            MoteService moteService,
            PdlConsumer pdlConsumer
    ) {
        this.narmesteLederConsumer = narmesteLederConsumer;
        this.moteService = moteService;
        this.pdlConsumer = pdlConsumer;
    }

    public List<Mote> hentNarmesteLedersMoter(String lederAktorId) {
        Fodselsnummer lederFnr = pdlConsumer.fodselsnummer(new AktorId(lederAktorId));

        List<NarmesteLederRelasjonDTO> narmesteLederRelasjoner = narmesteLederConsumer.getAnsatteUsingSystemToken(lederFnr.getValue());

        List<Mote> moter = new ArrayList<>();
        for (NarmesteLederRelasjonDTO narmesteLederRelasjon : narmesteLederRelasjoner) {
            AktorId innbyggerAktorId = pdlConsumer.aktorId(new Fodselsnummer(narmesteLederRelasjon.getArbeidstakerPersonIdentNumber()));
            List<Mote> ansattesMoter = moteService.findMoterByBrukerAktoerIdOgAGOrgnummer(innbyggerAktorId.getValue(), narmesteLederRelasjon.getVirksomhetsnummer());

            ansattesMoter.removeIf(mote -> mote
                    .alternativer
                    .stream()
                    .allMatch(aktivFomErEtterAlternativtidspunkt(narmesteLederRelasjon)));

            moter.addAll(ansattesMoter);
        }
        return moter;
    }

    private Predicate<TidOgSted> aktivFomErEtterAlternativtidspunkt(NarmesteLederRelasjonDTO ansatt) {
        return tidOgSted -> ansatt
                .getAktivFom()
                .isAfter(tidOgSted.tid.toLocalDate());
    }
}

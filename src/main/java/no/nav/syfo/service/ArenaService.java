package no.nav.syfo.service;

import no.nav.syfo.domain.model.Mote;
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.*;
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.meldinger.WSBestillOppgaveRequest;
import org.slf4j.Logger;

import javax.inject.Inject;

import static java.time.LocalDate.now;
import static no.nav.syfo.util.MoterUtil.finnAktoerFraMote;
import static org.slf4j.LoggerFactory.getLogger;

public class ArenaService {

    private static final Logger LOG = getLogger(ArenaService.class);

    @Inject
    private BehandleArbeidOgAktivitetOppgaveV1 behandleArbeidOgAktivitetOppgaveV1;
    @Inject
    private MoteService moteService;
    @Inject
    private AktoerService aktoerService;

    public void bestillOppgave(String moteUuid) {
        Mote mote = moteService.findMoteByUUID(moteUuid);
        try {
            String fnr = aktoerService.hentFnrForAktoer(finnAktoerFraMote(mote).aktorId);
            behandleArbeidOgAktivitetOppgaveV1.bestillOppgave(request(fnr, mote.navEnhet));
        } catch (BestillOppgaveSakIkkeOpprettet | BestillOppgaveOrganisasjonIkkeFunnet | BestillOppgaveSikkerhetsbegrensning
                | BestillOppgavePersonIkkeFunnet | BestillOppgavePersonErInaktiv | BestillOppgaveUgyldigInput e) {
            LOG.warn("Kunne ikke bestille sak", e);
            throw new RuntimeException();
        } catch (RuntimeException e) {
            LOG.error("Feil ved henting bestilling av oppgave", e);
            throw new RuntimeException();
        }
    }

    private WSBestillOppgaveRequest request(String brukerIdent, String behandlendeEnhetId) {
        return new WSBestillOppgaveRequest()
                .withOppgavetype(
                        new WSOppgavetype()
                                .withValue("OPPSANKSYK"))
                .withOppgave(
                        new WSOppgave()
                                .withBruker(new WSPerson().withIdent(brukerIdent))
                                .withFrist(now().plusDays(14))
                                .withBehandlendeEnhetId(behandlendeEnhetId)
                                .withTema(new WSTema().withValue("SYK"))
                                .withBeskrivelse("Vurder sanksjon - sykmeldt")
                                .withPrioritet(new WSPrioritet().withValue("HOY"))
                                .withTilleggsinformasjon("Opprettet via melding fra Modia Sykefravær"));
    }
}

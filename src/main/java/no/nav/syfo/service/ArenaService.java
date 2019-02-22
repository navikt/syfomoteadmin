package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.model.Mote;
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.*;
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.meldinger.WSBestillOppgaveRequest;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static java.time.LocalDate.now;
import static no.nav.syfo.util.MoterUtil.finnAktoerFraMote;

@Slf4j
@Service
public class ArenaService {

    private BehandleArbeidOgAktivitetOppgaveV1 behandleArbeidOgAktivitetOppgaveV1;

    private MoteService moteService;

    private AktoerService aktoerService;

    @Inject
    public ArenaService(
            BehandleArbeidOgAktivitetOppgaveV1 behandleArbeidOgAktivitetOppgaveV1,
            MoteService moteService,
            AktoerService aktoerService
    ) {
        this.behandleArbeidOgAktivitetOppgaveV1 = behandleArbeidOgAktivitetOppgaveV1;
        this.moteService = moteService;
        this.aktoerService = aktoerService;
    }

    public void bestillOppgave(String moteUuid) {
        Mote Mote = moteService.findMoteByUUID(moteUuid);
        try {
            String fnr = aktoerService.hentFnrForAktoer(finnAktoerFraMote(Mote).aktorId);
            behandleArbeidOgAktivitetOppgaveV1.bestillOppgave(request(fnr, Mote.navEnhet));
        } catch (BestillOppgaveSakIkkeOpprettet | BestillOppgaveOrganisasjonIkkeFunnet | BestillOppgaveSikkerhetsbegrensning
                | BestillOppgavePersonIkkeFunnet | BestillOppgavePersonErInaktiv | BestillOppgaveUgyldigInput e) {
            log.warn("Kunne ikke bestille sak", e);
            throw new RuntimeException();
        } catch (RuntimeException e) {
            log.error("Feil ved henting bestilling av oppgave", e);
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

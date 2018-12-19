package no.nav.syfo.config.mocks;

import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.*;
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.meldinger.WSBestillOppgaveRequest;
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.meldinger.WSBestillOppgaveResponse;

public class ArenaMock implements BehandleArbeidOgAktivitetOppgaveV1 {

    @Override
    public WSBestillOppgaveResponse bestillOppgave(WSBestillOppgaveRequest request) throws BestillOppgaveSikkerhetsbegrensning, BestillOppgaveOrganisasjonIkkeFunnet, BestillOppgavePersonErInaktiv, BestillOppgaveSakIkkeOpprettet, BestillOppgavePersonIkkeFunnet, BestillOppgaveUgyldigInput {
        return new WSBestillOppgaveResponse()
                .withArenaSakId("123456789")
                .withOppgaveId("1234");
    }

    @Override
    public void ping() {

    }
}

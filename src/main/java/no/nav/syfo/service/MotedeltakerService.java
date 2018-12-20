package no.nav.syfo.service;

import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.MotedeltakerAktorId;
import no.nav.syfo.domain.model.MotedeltakerArbeidsgiver;
import no.nav.syfo.repository.dao.MotedeltakerDAO;

import javax.inject.Inject;
import java.util.List;

public class MotedeltakerService {

    @Inject
    private MotedeltakerDAO motedeltakerDAO;

    public List<MotedeltakerArbeidsgiver> findMotedeltakereSomIkkeHarSvartSisteDognet(int antallDagerBakoverEkstra) {
        return motedeltakerDAO.findMotedeltakereSomIkkeHarSvartSisteDognet(antallDagerBakoverEkstra);
    }

    public static MotedeltakerAktorId finnAktoerIMote(Mote Mote) {
        return Mote.motedeltakere.stream()
                .filter(motedeltaker -> motedeltaker instanceof MotedeltakerAktorId)
                .map(motedeltaker -> (MotedeltakerAktorId) motedeltaker)
                .findAny().orElseThrow(() -> new RuntimeException("Fant ikke aktoeren i Møtet!"));
    }


    public void deltakerHarSvart(String motedeltakerUuid, List<Long> valgteAlternativer) {
        motedeltakerDAO.motedeltakerHarSvart(motedeltakerUuid, valgteAlternativer);
    }

    public List<String> sykmeldteMedMoteHvorBeggeHarSvart(String enhet) {
        return motedeltakerDAO.sykmeldteMedMoteHvorBeggeHarSvart(enhet);
    }

    public String finnArbeidstakerAktorIdForMoteId(Long moteId) {
        return motedeltakerDAO.arbeidstakerMotedeltakerAktorIdByMoteId(moteId).aktorId;
    }
}

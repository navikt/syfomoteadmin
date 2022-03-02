package no.nav.syfo.service;

import no.nav.syfo.domain.model.MotedeltakerArbeidsgiver;
import no.nav.syfo.repository.dao.MotedeltakerDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MotedeltakerService {

    private MotedeltakerDAO motedeltakerDAO;

    @Autowired
    public MotedeltakerService(
            MotedeltakerDAO motedeltakerDAO
    ) {
        this.motedeltakerDAO = motedeltakerDAO;
    }

    public List<MotedeltakerArbeidsgiver> findMotedeltakereSomIkkeHarSvartSisteDognet(int antallDagerBakoverEkstra) {
        return motedeltakerDAO.findMotedeltakereSomIkkeHarSvartSisteDognet(antallDagerBakoverEkstra);
    }

    public void deltakerHarSvart(String motedeltakerUuid, List<Long> valgteAlternativer) {
        motedeltakerDAO.motedeltakerHarSvart(motedeltakerUuid, valgteAlternativer);
    }

    public String finnArbeidstakerAktorIdForMoteId(Long moteId) {
        return motedeltakerDAO.arbeidstakerMotedeltakerAktorIdByMoteId(moteId).aktorId;
    }
}

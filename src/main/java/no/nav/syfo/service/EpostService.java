package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.repository.dao.EpostDAO;
import no.nav.syfo.repository.model.PEpost;
import no.nav.syfo.util.Toggle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class EpostService {

    private EpostDAO epostDAO;

    private Toggle toggle;

    @Autowired
    public EpostService(
            EpostDAO epostDAO,
            Toggle toggle
    ) {
        this.epostDAO = epostDAO;
        this.toggle = toggle;
    }

    @Transactional
    public void klargjorForSending(PEpost epost) {
        if (!toggle.toggleSendeEpost()) {
            log.info("Sender ikke epost fordi det er togglet av!");
            return;
        }

        long epostId = epostDAO.create(epost);
        epost.vedlegg.forEach(vedlegg -> epostDAO.create(vedlegg.epostId(epostId)));
    }
}

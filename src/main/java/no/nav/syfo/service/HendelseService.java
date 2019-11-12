package no.nav.syfo.service;

import no.nav.syfo.domain.model.*;
import no.nav.syfo.repository.dao.HendelseDAO;
import no.nav.syfo.repository.model.PHendelseVarselMotedeltaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static no.nav.syfo.domain.model.HendelseVarselMotedeltaker.Resultat.OK;
import static no.nav.syfo.domain.model.HendelsesType.MOTESTATUS_ENDRET;
import static no.nav.syfo.domain.model.HendelsesType.VARSEL;

@Service
public class HendelseService {

    private HendelseDAO hendelseDAO;

    @Autowired
    public HendelseService(
            HendelseDAO hendelseDAO
    ) {
        this.hendelseDAO = hendelseDAO;
    }

    public Optional<LocalDateTime> sistEndretMoteStatus(long moteId) {
        return hendelseDAO.moteStatusEndretHendelser(moteId)
                .stream()
                .sorted((o1, o2) -> o2.inntruffetdato.compareTo(o1.inntruffetdato))
                .map(hendelse -> hendelse.inntruffetdato)
                .findFirst();
    }

    public void opprettHendelseVarselArbeidsgiver(Varseltype type, MotedeltakerArbeidsgiver arbeidsgiver, String innloggetIdent) {
        String opprettetAv = innloggetIdent;
        hendelseDAO.create(new PHendelseVarselMotedeltaker()
                .opprettetAv(opprettetAv)
                .resultat(OK.name())
                .varseltype(type.name())
                .type(VARSEL.name())
                .motedeltakerId(arbeidsgiver.id)
                .kanal(Kanal.EPOST.name())
                .adresse(arbeidsgiver.epost)
        );
    }

    void moteStatusEndret(Mote mote, String innloggetIdent) {
        HendelseMoteStatusEndret hendelse = new HendelseMoteStatusEndret()
                .type(MOTESTATUS_ENDRET)
                .moteId(mote.id)
                .inntruffetdato(now())
                .opprettetAv(innloggetIdent)
                .status(mote.status);
        hendelseDAO.create(hendelse);
    }

}

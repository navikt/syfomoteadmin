package no.nav.syfo.service;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.domain.model.*;
import no.nav.syfo.repository.dao.HendelseDAO;
import no.nav.syfo.repository.model.PHendelseVarselMotedeltaker;
import no.nav.syfo.repository.model.PHendelseVarselVeileder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.util.Optional.ofNullable;
import static no.nav.syfo.domain.model.HendelseVarselMotedeltaker.Resultat.OK;
import static no.nav.syfo.domain.model.HendelsesType.MOTESTATUS_ENDRET;
import static no.nav.syfo.domain.model.HendelsesType.VARSEL;
import static no.nav.syfo.util.OIDCUtil.getSubjectIntern;

@Service
public class HendelseService {

    private final String SRV_BRUKER = "srvmoteadmin";

    private OIDCRequestContextHolder contextHolder;

    private HendelseDAO hendelseDAO;

    @Autowired
    public HendelseService(
            OIDCRequestContextHolder contextHolder,
            HendelseDAO hendelseDAO
    ) {
        this.contextHolder = contextHolder;
        this.hendelseDAO = hendelseDAO;
    }

    public Optional<LocalDateTime> sistEndretMoteStatus(long moteId) {
        return hendelseDAO.moteStatusEndretHendelser(moteId)
                .stream()
                .sorted((o1, o2) -> o2.inntruffetdato.compareTo(o1.inntruffetdato))
                .map(hendelse -> hendelse.inntruffetdato)
                .findFirst();
    }

    public void opprettHendelseVarselArbeidsgiver(Varseltype type, MotedeltakerArbeidsgiver arbeidsgiver, boolean erSystemKall) {
        String opprettetAv = erSystemKall ? SRV_BRUKER : opprettetAv(contextHolder);
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

    public void opprettHendelseVarselVeileder(Varseltype type, Veileder veileder) {
        hendelseDAO.create(new PHendelseVarselVeileder()
                .opprettetAv(opprettetAv(contextHolder))
                .varseltype(type.name())
                .type(VARSEL.name())
                .kanal(Kanal.EPOST.name())
                .moteId(veileder.mote.id)
                .veilederident(veileder.userId)
        );
    }

    private String opprettetAv(OIDCRequestContextHolder ctxHolder) {
        return ofNullable(getSubjectIntern(ctxHolder)).orElse(SRV_BRUKER);
    }

    void moteStatusEndret(Mote Mote) {
        HendelseMoteStatusEndret hendelse = new HendelseMoteStatusEndret()
                .type(MOTESTATUS_ENDRET)
                .moteId(Mote.id)
                .inntruffetdato(now())
                .opprettetAv(opprettetAv(contextHolder))
                .status(Mote.status);
        hendelseDAO.create(hendelse);
    }

}

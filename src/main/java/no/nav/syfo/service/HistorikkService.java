package no.nav.syfo.service;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.aktorregister.domain.AktorId;
import no.nav.syfo.api.domain.RSHistorikk;
import no.nav.syfo.domain.model.*;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.repository.dao.HendelseDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class HistorikkService {

    private final AktorregisterConsumer aktorregisterConsumer;
    private final PdlConsumer pdlConsumer;
    private final HendelseDAO hendelseDAO;

    @Autowired
    public HistorikkService(
            AktorregisterConsumer aktorregisterConsumer,
            PdlConsumer pdlConsumer,
            HendelseDAO hendelseDAO
    ) {
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.pdlConsumer = pdlConsumer;
        this.hendelseDAO = hendelseDAO;
    }


    public List<RSHistorikk> opprettetHistorikk(List<Mote> moter) {
        return moter
                .stream()
                .map(mote -> new RSHistorikk()
                        .tidspunkt(mote.opprettetTidspunkt)
                        .opprettetAv(mote.opprettetAv)
                        .tekst(mote.opprettetAv + " opprettet møte" + motedeltakereNavn(mote.motedeltakere, " med "))
                )
                .collect(toList());
    }

    public List<RSHistorikk> flereTidspunktHistorikk(List<Mote> moter) {
        return moter
                .stream()
                .map(mote -> hendelseDAO.moteStatusEndretHendelser(mote.id).stream()
                        .filter(hendelse -> hendelse.status.name().equals(MoteStatus.FLERE_TIDSPUNKT.name()))
                        .map(hendelse -> new RSHistorikk()
                                .tidspunkt(hendelse.inntruffetdato)
                                .opprettetAv(hendelse.opprettetAv)
                                .tekst(hendelse.opprettetAv + " la til flere tidspunkt" + motedeltakereNavn(mote.motedeltakere, " til "))
                        ).collect(toList()))
                .flatMap(Collection::stream)
                .collect(toList());
    }

    public List<RSHistorikk> avbruttHistorikk(List<Mote> moter) {
        return moter
                .stream()
                .map(mote -> hendelseDAO.moteStatusEndretHendelser(mote.id).stream()
                        .filter(hendelse -> hendelse.status.name().equals(MoteStatus.AVBRUTT.name()))
                        .collect(toList()))
                .flatMap(Collection::stream)
                .map(hendelse -> new RSHistorikk()
                        .tidspunkt(hendelse.inntruffetdato)
                        .opprettetAv(hendelse.opprettetAv)
                        .tekst(hendelse.opprettetAv + " avbrøt møteforespørselen")
                )
                .collect(toList());
    }

    public List<RSHistorikk> bekreftetHistorikk(List<Mote> moter) {
        return moter
                .stream()
                .map(mote -> hendelseDAO.moteStatusEndretHendelser(mote.id).stream()
                        .filter(hendelse -> hendelse.status.name().equals(MoteStatus.BEKREFTET.name()))
                        .collect(toList()))
                .flatMap(Collection::stream)
                .map(hendelse -> new RSHistorikk()
                        .tidspunkt(hendelse.inntruffetdato)
                        .opprettetAv(hendelse.opprettetAv)
                        .tekst(hendelse.opprettetAv + " bekreftet møteforespørselen")
                )
                .collect(toList());
    }


    //NB! Hvis fastlege eller lignende skal kobles på => skriv denne om til mer generisk.
    private String motedeltakereNavn(List<Motedeltaker> motedeltakere, String bindeord) {
        if (motedeltakere.size() == 1) {
            return bindeord + motedeltakere.get(0).navn;
        } else {
            return bindeord + hentNavn(motedeltakere.get(0)) + " og " + hentNavn(motedeltakere.get(1));
        }
    }

    private String hentNavn(Motedeltaker motedeltaker) {
        if (motedeltaker.motedeltakertype.equals("Bruker")) {
            MotedeltakerAktorId motedeltakerAktorId = (MotedeltakerAktorId) motedeltaker;
            return pdlConsumer.fullName(aktorregisterConsumer.getFnrForAktorId(new AktorId(motedeltakerAktorId.aktorId)));
        }
        return motedeltaker.navn;
    }
}

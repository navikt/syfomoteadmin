package no.nav.syfo.history

import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.model.*
import no.nav.syfo.history.controller.RSHistorikk
import no.nav.syfo.repository.dao.HendelseDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class HistorikkService @Autowired constructor(
    private val pdlConsumer: PdlConsumer,
    private val hendelseDAO: HendelseDAO
) {
    fun opprettetHistorikk(moter: List<Mote>): List<RSHistorikk> {
        return moter
            .map { mote: Mote ->
                RSHistorikk(
                    tidspunkt = mote.opprettetTidspunkt,
                    opprettetAv = mote.opprettetAv,
                    tekst = mote.opprettetAv + " opprettet møte" + motedeltakereNavn(mote.motedeltakere, " med ")
                )
            }
    }

    fun flereTidspunktHistorikk(moter: List<Mote>): List<RSHistorikk> {
        return moter
            .map { mote: Mote ->
                hendelseDAO.moteStatusEndretHendelser(mote.id)
                    .filter { hendelse: HendelseMoteStatusEndret -> hendelse.status.name == MoteStatus.FLERE_TIDSPUNKT.name }
                    .map { hendelse: HendelseMoteStatusEndret ->
                        RSHistorikk(
                            tidspunkt = hendelse.inntruffetdato,
                            opprettetAv = hendelse.opprettetAv,
                            tekst = hendelse.opprettetAv + " la til flere tidspunkt" + motedeltakereNavn(mote.motedeltakere, " til ")
                        )
                    }
            }
            .flatten()
    }

    fun avbruttHistorikk(moter: List<Mote>): List<RSHistorikk> {
        return moter
            .map { mote: Mote ->
                hendelseDAO.moteStatusEndretHendelser(mote.id).stream()
                    .filter { hendelse: HendelseMoteStatusEndret -> hendelse.status.name == MoteStatus.AVBRUTT.name }
                    .collect(Collectors.toList())
            }
            .flatten()
            .map { hendelse: HendelseMoteStatusEndret ->
                RSHistorikk(
                    tidspunkt = hendelse.inntruffetdato,
                    opprettetAv = hendelse.opprettetAv,
                    tekst = hendelse.opprettetAv + " avbrøt møteforespørselen"
                )
            }
    }

    fun bekreftetHistorikk(moter: List<Mote>): List<RSHistorikk> {
        return moter
            .map { mote: Mote ->
                hendelseDAO.moteStatusEndretHendelser(mote.id).stream()
                    .filter { hendelse: HendelseMoteStatusEndret -> hendelse.status.name == MoteStatus.BEKREFTET.name }
                    .collect(Collectors.toList())
            }
            .flatten()
            .map { hendelse: HendelseMoteStatusEndret ->
                RSHistorikk(
                    tidspunkt = hendelse.inntruffetdato,
                    opprettetAv = hendelse.opprettetAv,
                    tekst = hendelse.opprettetAv + " bekreftet møteforespørselen"
                )
            }
    }

    //NB! Hvis fastlege eller lignende skal kobles på => skriv denne om til mer generisk.
    private fun motedeltakereNavn(motedeltakere: List<Motedeltaker>, bindeord: String): String {
        return if (motedeltakere.size == 1) {
            bindeord + motedeltakere[0].navn
        } else {
            bindeord + hentNavn(motedeltakere[0]) + " og " + hentNavn(motedeltakere[1])
        }
    }

    private fun hentNavn(motedeltaker: Motedeltaker): String {
        if (motedeltaker.motedeltakertype == "Bruker") {
            val motedeltakerAktorId = motedeltaker as MotedeltakerAktorId
            return pdlConsumer.fullName(pdlConsumer.fodselsnummer(AktorId(motedeltakerAktorId.aktorId)).value)
        }
        return motedeltaker.navn
    }
}

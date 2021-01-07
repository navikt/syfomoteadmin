package no.nav.syfo.oversikthendelse

import no.nav.syfo.consumer.behandlendeenhet.BehandlendeEnhetConsumer
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.domain.model.Mote
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OversikthendelseService(
    private val behandlendeEnhetConsumer: BehandlendeEnhetConsumer,
    private val pdlConsumer: PdlConsumer,
    private val oversikthendelseProducer: OversikthendelseProducer
) {
    fun sendOversikthendelse(mote: Mote, type: OversikthendelseType) {
        val fnr = pdlConsumer.fodselsnummer(AktorId(mote.sykmeldt().aktorId))
        val behandlendeEnhet = behandlendeEnhetConsumer.getBehandlendeEnhet(fnr.value, null).enhetId

        val kOversikthendelse = map2KOversikthendelse(fnr, behandlendeEnhet, type)
        oversikthendelseProducer.sendOversikthendelse(mote.uuid, kOversikthendelse)
    }

    private fun map2KOversikthendelse(
        fnr: Fodselsnummer,
        tildeltEnhet: String,
        oversikthendelseType: OversikthendelseType
    ) = KOversikthendelse(
        fnr = fnr.value,
        hendelseId = oversikthendelseType.name,
        enhetId = tildeltEnhet,
        tidspunkt = LocalDateTime.now()
    )
}

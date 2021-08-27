package no.nav.syfo.testhelper

import no.nav.syfo.api.ressurser.azuread.v2.EmailContentControllerV2
import no.nav.syfo.domain.model.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID
import java.time.LocalDateTime
import java.util.*

class MoteGenerator {
    private val tidOgSted = TidOgSted()
        .id(1L)
        .tid(LocalDateTime.now().plusMonths(1L))
        .sted("Nav")

    private val moteDeltaker: Motedeltaker = MotedeltakerAktorId()
        .uuid(UUID.randomUUID().toString())
        .aktorId(ARBEIDSTAKER_AKTORID)
        .motedeltakertype(EmailContentControllerV2.BRUKER)
        .tidOgStedAlternativer(listOf(tidOgSted))

    private val mote = Mote()
        .motedeltakere(listOf(moteDeltaker))

    fun generateMote(): Mote {
        return mote
    }

    fun generateMote(uuid: UUID): Mote {
        return mote
            .motedeltakere(listOf(moteDeltaker.uuid(uuid.toString())))
    }
}

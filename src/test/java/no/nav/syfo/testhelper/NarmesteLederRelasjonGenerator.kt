package no.nav.syfo.testhelper

import no.nav.syfo.narmesteleder.NarmesteLederRelasjon
import no.nav.syfo.testhelper.UserConstants.LEDER_AKTORID
import java.time.LocalDate

fun generateNarmesteLederRelasjon(): NarmesteLederRelasjon {
    return NarmesteLederRelasjon(
            aktorId = "aktoerId1",
            orgnummer = "orgnummer1",
            narmesteLederAktorId = LEDER_AKTORID,
            narmesteLederTelefonnummer = null,
            narmesteLederEpost = null,
            aktivFom = LocalDate.of(2017, 3, 2),
            arbeidsgiverForskutterer = false,
            skrivetilgang = false,
            tilganger = emptyList()
    )
}

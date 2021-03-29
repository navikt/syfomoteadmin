package no.nav.syfo.testhelper

import no.nav.syfo.consumer.narmesteleder.NarmesteLederRelasjon
import no.nav.syfo.consumer.narmesteleder.Tilgang
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
            aktivTom = LocalDate.of(2018, 3, 2),
            arbeidsgiverForskutterer = false,
            skrivetilgang = false,
            tilganger = emptyList()
    )
}

fun generateNarmesteLederRelasjon(aktorId: String = "aktoerId1",
                                  orgnummer: String = "orgnummer1",
                                  narmesteLederAktorId: String = LEDER_AKTORID,
                                  narmesteLederTelefonnummer: String? = null,
                                  narmesteLederEpost: String? = null,
                                  aktivFom: LocalDate = LocalDate.of(2017, 3, 2),
                                  aktivTom: LocalDate = LocalDate.of(2018, 3, 2),
                                  arbeidsgiverForskutterer: Boolean? = false,
                                  skrivetilgang: Boolean = false,
                                  tilganger: List<Tilgang> = emptyList()): NarmesteLederRelasjon {

    return NarmesteLederRelasjon(
            aktorId,
            orgnummer,
            narmesteLederAktorId,
            narmesteLederTelefonnummer,
            narmesteLederEpost,
            aktivFom,
            aktivTom,
            arbeidsgiverForskutterer,
            skrivetilgang,
            tilganger
    )
}

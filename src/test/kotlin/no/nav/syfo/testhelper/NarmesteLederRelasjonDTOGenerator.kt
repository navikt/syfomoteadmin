package no.nav.syfo.testhelper

import no.nav.syfo.consumer.narmesteleder.NarmesteLederRelasjonDTO
import java.time.LocalDate
import java.time.LocalDateTime

const val innbyggerIdent = "10107012345"
const val activeLederIdent = "01018098765"
const val inactiveLederIdent = "01018033333"
const val wrongLederIdent = "01018014785"
const val activeVirksomhetsnummer = "12345"
const val wrongVirksomhetsnummer = "98765"

val activeLeder = NarmesteLederRelasjonDTO(
    uuid = "123",
    arbeidstakerPersonIdentNumber = innbyggerIdent,
    virksomhetsnavn = "FBI",
    virksomhetsnummer = activeVirksomhetsnummer,
    narmesteLederPersonIdentNumber = activeLederIdent,
    narmesteLederTelefonnummer = "12345678",
    narmesteLederEpost = "skinman@fbi.no",
    narmesteLederNavn = "Walter Skinner",
    aktivFom = LocalDate.now().minusYears(1),
    aktivTom = null,
    arbeidsgiverForskutterer = true,
    timestamp = LocalDateTime.now(),
    status = "INNMELDT_AKTIV",
)

val activeLederRelasjonWithInnbyggerAsLeder = activeLeder.copy(
    arbeidstakerPersonIdentNumber = wrongLederIdent,
    narmesteLederPersonIdentNumber = innbyggerIdent,
)

val inactiveLeder = NarmesteLederRelasjonDTO(
    uuid = "987",
    arbeidstakerPersonIdentNumber = innbyggerIdent,
    virksomhetsnavn = "FBI",
    virksomhetsnummer = activeVirksomhetsnummer,
    narmesteLederPersonIdentNumber = inactiveLederIdent,
    narmesteLederTelefonnummer = "87654321",
    narmesteLederEpost = "hoover@fbi.no",
    narmesteLederNavn = "J. Edgar Hoover",
    aktivFom = LocalDate.now().minusYears(10),
    aktivTom = LocalDate.now().minusYears(1),
    arbeidsgiverForskutterer = true,
    timestamp = LocalDateTime.now(),
    status = "DEAKTIVERT_NY_LEDER",
)

val activeLederWrongVirksomhet = NarmesteLederRelasjonDTO(
    uuid = "546",
    arbeidstakerPersonIdentNumber = innbyggerIdent,
    virksomhetsnavn = "Syndicate",
    virksomhetsnummer = wrongVirksomhetsnummer,
    narmesteLederPersonIdentNumber = wrongLederIdent,
    narmesteLederTelefonnummer = "99955511",
    narmesteLederEpost = "smoking.man@syndicate.no",
    narmesteLederNavn = "C.G.B. Spender",
    aktivFom = LocalDate.now().minusYears(40),
    aktivTom = null,
    arbeidsgiverForskutterer = false,
    timestamp = LocalDateTime.now(),
    status = "INNMELDT_AKTIV",
)

val lederListWithActiveLeder = listOf(inactiveLeder, activeLeder, activeLederWrongVirksomhet)
val lederListWithoutActiveLeder = listOf(inactiveLeder, activeLederWrongVirksomhet)
val listWithIdentAsBothLederAndAnsatt = listOf(inactiveLeder, activeLeder, activeLederWrongVirksomhet, activeLederRelasjonWithInnbyggerAsLeder)

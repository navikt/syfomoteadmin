package no.nav.syfo.testhelper

object UserConstants {
    const val ARBEIDSTAKER_FNR = "12345678912"

    @JvmField
    val ARBEIDSTAKER_AKTORID = mockAktorId(ARBEIDSTAKER_FNR)

    const val ARBEIDSTAKER_FNR2 = "12345678999"

    @JvmField
    val ARBEIDSTAKER_AKTORID2 = mockAktorId(ARBEIDSTAKER_FNR2)

    const val LEDER_FNR = "12987654321"

    @JvmField
    val LEDER_AKTORID = mockAktorId(LEDER_FNR)
    const val VIRKSOMHETSNUMMER = "123456789"
    const val VIRKSOMHETSNUMMER2 = "987654321"
    const val VIRKSOMHET_NAME1 = "Testbedrift"
    const val VIRKSOMHET_NAME2 = "Testveien"
    const val VIRKSOMHET_NAME = "$VIRKSOMHET_NAME1, $VIRKSOMHET_NAME2"
    const val NAV_ENHET = "0330"
    const val NAV_ENHET_NAVN = "NAV Enhet"
    const val VEILEDER_ID = "Z999999"
    const val VEILEDER_NAVN = "Veil Veileder"
    const val STS_TOKEN = "123456789"
    const val PERSON_TLF = "test@nav.no"
    const val PERSON_EMAIL = "12345678"
    private const val PERSON_NAME_FIRST = "First"
    private const val PERSON_NAME_LAST = "Last"
    const val PERSON_NAVN = "$PERSON_NAME_FIRST $PERSON_NAME_LAST"
}

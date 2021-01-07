package no.nav.syfo.controller.internad.person

data class RSReservasjon (
    val skalHaVarsel: Boolean? = null,
    val feilAarsak: KontaktInfoFeilAarsak? = null
)

enum class KontaktInfoFeilAarsak {
    RESERVERT,
    KODE6,
    INGEN_KONTAKTINFORMASJON,
    UTGAATT
}

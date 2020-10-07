package no.nav.syfo.consumer.dkif

data class DigitalKontaktinfoBolk (
        val feil: Map<String, Feil>? = null,
        val kontaktinfo: Map<String, DigitalKontaktinfo>? = null
)

data class DigitalKontaktinfo(
        val epostadresse: String? = null,
        val kanVarsles: Boolean,
        val reservert: Boolean? = null,
        val mobiltelefonnummer: String? = null,
        val personident: String
)

data class Feil(
        val melding: String
)

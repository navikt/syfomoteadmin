package no.nav.syfo.dkif

data class DigitalKontaktinfoBolk (
        val feil: Map<String, Feil>?,
        val kontaktinfo: Map<String, DigitalKontaktinfo>?
)

data class DigitalKontaktinfo(
        val epostadresse: String,
        val kanVarsles: Boolean,
        val reservert: Boolean,
        val mobiltelefonnummer: String,
        val personident: String
)

data class Feil(
        val melding: String
)

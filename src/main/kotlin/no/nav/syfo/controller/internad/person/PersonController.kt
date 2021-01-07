package no.nav.syfo.controller.internad.person

import no.nav.security.oidc.api.ProtectedWithClaims
import no.nav.syfo.api.auth.OIDCIssuer.AZURE
import no.nav.syfo.consumer.dkif.DkifConsumer
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.consumer.veiledertilgang.VeilederTilgangConsumer
import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.metric.Metric
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/internad/brukerinfo/{ident}"])
@ProtectedWithClaims(issuer = AZURE)
class PersonController @Inject constructor(
    private val dkifConsumer: DkifConsumer,
    private val pdlConsumer: PdlConsumer,
    private val tilgangService: VeilederTilgangConsumer,
    private val metric: Metric
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @RequestMapping(value = ["/navn"])
    fun hentBruker(@PathVariable("ident") ident: String): RSBruker {
        metric.tellEndepunktKall("bruker_navn")
        val fnr = getFnrForIdent(ident)
        tilgangService.throwExceptionIfVeilederWithoutAccess(fnr)
        return RSBruker(
            navn = pdlConsumer.fullName(fnr.value),
            kontaktinfo = null
        )
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun bruker(@PathVariable("ident") ident: String): RSBruker {
        metric.tellEndepunktKall("bruker")
        val fnr = getFnrForIdent(ident)
        tilgangService.throwExceptionIfVeilederWithoutAccess(fnr)
        val kontaktinfo = dkifConsumer.kontaktinformasjon(fnr.value)
        return RSBruker(
            navn = pdlConsumer.fullName(fnr.value),
            kontaktinfo = RSKontaktinfo(
                tlf = kontaktinfo.mobiltelefonnummer,
                epost = kontaktinfo.epostadresse,
                reservasjon = RSReservasjon(
                    skalHaVarsel = kontaktinfo.kanVarsles,
                    feilAarsak = null
                )
            )
        )
    }

    private fun getFnrForIdent(@PathVariable("ident") ident: String): Fodselsnummer {
        return if (ident.matches(Regex("\\d{11}$"))) {
            Fodselsnummer(ident)
        } else {
            pdlConsumer.fodselsnummer(AktorId(ident))
        }
    }
}

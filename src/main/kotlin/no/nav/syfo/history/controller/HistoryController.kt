package no.nav.syfo.history.controller

import no.nav.security.oidc.api.ProtectedWithClaims
import no.nav.syfo.api.auth.OIDCIssuer.AZURE
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.consumer.veiledertilgang.VeilederTilgangConsumer
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.history.HistorikkService
import no.nav.syfo.service.MoteService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = AZURE)
@RequestMapping(value = ["/api/internad/historikk"])
class HistoryController @Inject constructor(
    private val tilgangService: VeilederTilgangConsumer,
    private val moteService: MoteService,
    private val pdlConsumer: PdlConsumer,
    private val historikkService: HistorikkService
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getHistory(
        @RequestParam(value = "fnr") fnr: String
    ): List<RSHistorikk> {
        val personFnr = Fodselsnummer(fnr)

        tilgangService.throwExceptionIfVeilederWithoutAccess(personFnr)

        val moter = moteService.findMoterByBrukerAktoerId(pdlConsumer.aktorId(personFnr).value)

        return historikkService.opprettetHistorikk(moter) +
            historikkService.flereTidspunktHistorikk(moter) +
            historikkService.avbruttHistorikk(moter) +
            historikkService.bekreftetHistorikk(moter)
    }
}

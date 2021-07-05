package no.nav.syfo.history.controller.v2

import no.nav.security.oidc.api.ProtectedWithClaims
import no.nav.syfo.api.auth.OIDCIssuer.VEILEDER_AZURE_V2
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.consumer.veiledertilgang.VeilederTilgangConsumer
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.history.HistorikkService
import no.nav.syfo.history.controller.RSHistorikk
import no.nav.syfo.service.MoteService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = VEILEDER_AZURE_V2)
@RequestMapping(value = ["/api/internad/v2/historikk"])
class HistoryControllerV2 @Inject constructor(
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

        tilgangService.throwExceptionIfDeniedAccessAzureOBO(personFnr)

        val moter = moteService.findMoterByBrukerAktoerId(pdlConsumer.aktorId(personFnr).value)

        return historikkService.opprettetHistorikk(moter) +
            historikkService.flereTidspunktHistorikk(moter) +
            historikkService.avbruttHistorikk(moter) +
            historikkService.bekreftetHistorikk(moter)
    }
}

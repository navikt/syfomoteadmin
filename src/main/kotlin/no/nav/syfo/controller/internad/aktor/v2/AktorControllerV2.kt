package no.nav.syfo.controller.internad.aktor.v2

import no.nav.security.oidc.api.ProtectedWithClaims
import no.nav.syfo.api.auth.OIDCIssuer.VEILEDER_AZURE_V2
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.consumer.veiledertilgang.VeilederTilgangConsumer
import no.nav.syfo.controller.internad.aktor.RSAktor
import no.nav.syfo.domain.AktorId
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/internad/v2/aktor/{aktorId}"])
@ProtectedWithClaims(issuer = VEILEDER_AZURE_V2)
class AktorControllerV2 @Inject constructor(
    private val pdlConsumer: PdlConsumer,
    private val tilgangService: VeilederTilgangConsumer
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun get(@PathVariable("aktorId") aktorId: String): RSAktor {
        val fnr = pdlConsumer.fodselsnummer(AktorId(aktorId))

        tilgangService.throwExceptionIfDeniedAccessAzureOBO(fnr)

        return RSAktor(fnr = fnr.value)
    }
}

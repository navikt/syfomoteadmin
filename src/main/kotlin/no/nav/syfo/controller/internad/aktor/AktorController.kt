package no.nav.syfo.controller.internad.aktor

import no.nav.security.oidc.api.ProtectedWithClaims
import no.nav.syfo.api.auth.OIDCIssuer.AZURE
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.consumer.veiledertilgang.VeilederTilgangConsumer
import no.nav.syfo.domain.AktorId
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/internad/aktor/{aktorId}"])
@ProtectedWithClaims(issuer = AZURE)
class AktorController @Inject constructor(
    private val pdlConsumer: PdlConsumer,
    private val tilgangService: VeilederTilgangConsumer
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun get(@PathVariable("aktorId") aktorId: String): RSAktor {
        val fnr = pdlConsumer.fodselsnummer(AktorId(aktorId))

        tilgangService.throwExceptionIfVeilederWithoutAccess(fnr)

        return RSAktor(fnr = fnr.value)
    }
}

package no.nav.syfo.controller.internad.virksomhet.v2

import no.nav.security.oidc.api.ProtectedWithClaims
import no.nav.syfo.api.auth.OIDCIssuer.VEILEDER_AZURE_V2
import no.nav.syfo.consumer.ereg.EregConsumer
import no.nav.syfo.controller.internad.virksomhet.RSVirksomhet
import no.nav.syfo.domain.Virksomhetsnummer
import org.apache.commons.lang3.text.WordUtils.capitalize
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/internad/v2/virksomhet/{virksomhetsnummer}"])
@ProtectedWithClaims(issuer = VEILEDER_AZURE_V2)
class VirksomhetControllerV2 @Inject constructor(
    private val eregConsumer: EregConsumer
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getVirksomhetsnavn(
        @PathVariable("virksomhetsnummer") virksomhetsnummer: Virksomhetsnummer
    ): RSVirksomhet {
        return RSVirksomhet(
            navn = capitalize(
                eregConsumer.virksomhetsnavn(virksomhetsnummer).toLowerCase()
            )
        )
    }
}

package no.nav.syfo.controller.internad.virksomhet

import no.nav.security.oidc.api.ProtectedWithClaims
import no.nav.syfo.consumer.ereg.EregConsumer
import no.nav.syfo.api.auth.OIDCIssuer.AZURE
import no.nav.syfo.domain.Virksomhetsnummer
import org.apache.commons.lang3.text.WordUtils.capitalize
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/internad/virksomhet/{orgnummer}"])
@ProtectedWithClaims(issuer = AZURE)
class VirksomhetController @Inject constructor(
    private val eregConsumer: EregConsumer
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getVirksomhetsnavn(@PathVariable("orgnummer") orgnummer: Virksomhetsnummer): RSVirksomhet {
        return RSVirksomhet(
            navn = capitalize(eregConsumer.virksomhetsnavn(orgnummer).toLowerCase())
        )
    }
}

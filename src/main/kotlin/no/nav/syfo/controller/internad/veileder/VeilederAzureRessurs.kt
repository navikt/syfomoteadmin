package no.nav.syfo.controller.internad.veileder

import no.nav.security.oidc.api.ProtectedWithClaims
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.consumer.axsys.AxsysConsumer
import no.nav.syfo.consumer.axsys.AxsysEnhet
import no.nav.syfo.api.auth.OIDCIssuer
import no.nav.syfo.service.VeilederService
import no.nav.syfo.api.auth.OIDCUtil
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.util.stream.Collectors.toList
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/internad/veilederinfo"])
@ProtectedWithClaims(issuer = OIDCIssuer.AZURE)
class VeilederAzureRessurs @Inject constructor(
        private val contextHolder: OIDCRequestContextHolder,
        private val axsysConsumer: AxsysConsumer,
        private val veilederService: VeilederService
) {
    @GetMapping
    fun hentNavn(): RSVeilederInfo {
        return hentIdent(OIDCUtil.getSubjectInternAzure(contextHolder))
    }

    @GetMapping(value = ["/{ident}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentIdent(@PathVariable("ident") ident: String): RSVeilederInfo {
        return RSVeilederInfo(
                navn = veilederService.hentVeilederNavn(ident).orElse("Fant ikke navn"),
                ident = ident
        )
    }

    @GetMapping(value = ["/enheter"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentEnheter(): RSEnheter {
        val axsysEnhetList = axsysConsumer.enheter(OIDCUtil.getSubjectInternAzure(contextHolder))
        return mapAxsysEnhet2RS(axsysEnhetList)
    }

    fun mapAxsysEnhet2RS(axsysEnhetList: List<AxsysEnhet>): RSEnheter {
        val enhetList = axsysEnhetList
                .stream()
                .map {
                    RSEnhet(
                            enhetId = it.enhetId,
                            navn = it.navn
                    )
                }
                .collect(toList())
        return RSEnheter(enhetliste = enhetList)
    }
}

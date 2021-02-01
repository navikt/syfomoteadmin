package no.nav.syfo.controller.internad

import no.nav.security.oidc.api.ProtectedWithClaims
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.api.domain.nyttmoterequest.RSNyttAlternativ
import no.nav.syfo.api.mappers.RSNyttMoteMapper.opprett2TidOgSted
import no.nav.syfo.domain.model.TidOgSted
import no.nav.syfo.metric.Metric
import no.nav.syfo.api.auth.OIDCIssuer.AZURE
import no.nav.syfo.service.MoteService
import no.nav.syfo.util.MapUtil.mapListe
import no.nav.syfo.api.auth.OIDCUtil.getSubjectInternAzure
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = AZURE)
@RequestMapping(value = ["/api/internad/moter/{moteUuid}"])
class MoteActionsController @Inject
constructor(
    private val contextHolder: OIDCRequestContextHolder,
    private val metric: Metric,
    private val moteService: MoteService
) {

    @PostMapping(consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    @RequestMapping(value = ["/avbryt"])
    fun avbryt(
            @PathVariable("moteUuid") moteUuid: String,
            @RequestParam(value = "varsle") varsle: Boolean
    ) {
        moteService.avbrytMote(
                moteUuid,
                varsle,
                getSubjectInternAzure(contextHolder)
        )

        metric.tellEndepunktKall("avbryt_mote")
    }

    @PostMapping(consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    @RequestMapping(value = ["/bekreft"])
    fun bekreft(
        @PathVariable("moteUuid") moteUuid: String,
        @RequestParam(value = "valgtAlternativId") tidOgStedId: Long,
        @RequestParam(value = "varsle") varsle: Boolean?
    ) {
        moteService.bekreftMote(
                moteUuid,
                tidOgStedId,
                varsle ?: true,
                getSubjectInternAzure(contextHolder)
        )

        metric.tellEndepunktKall("bekreft_mote")
    }

    @PostMapping(consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    @RequestMapping(value = ["/nyealternativer"])
    fun nyeAlternativer(
            @PathVariable("moteUuid") moteUuid: String,
            @RequestBody alternativer: List<RSNyttAlternativ>
    ) {
        moteService.nyeAlternativer(
                moteUuid,
                mapListe<RSNyttAlternativ, TidOgSted>(alternativer, opprett2TidOgSted),
                getSubjectInternAzure(contextHolder)
        )

        metric.tellEndepunktKall("nye_alternativer")
    }
}

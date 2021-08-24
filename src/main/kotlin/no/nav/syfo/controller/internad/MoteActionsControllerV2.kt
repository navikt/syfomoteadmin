package no.nav.syfo.controller.internad

import no.nav.security.oidc.api.ProtectedWithClaims
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.api.auth.OIDCIssuer.VEILEDER_AZURE_V2
import no.nav.syfo.api.auth.OIDCUtil.getSubjectInternAzureV2
import no.nav.syfo.api.domain.nyttmoterequest.RSNyttAlternativ
import no.nav.syfo.api.mappers.RSNyttMoteMapper.opprett2TidOgSted
import no.nav.syfo.metric.Metric
import no.nav.syfo.service.MoteService
import no.nav.syfo.util.MapUtil.mapListe
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = VEILEDER_AZURE_V2)
@RequestMapping(value = ["/api/internad/v2/moter/{moteUuid}"])
class MoteActionsControllerV2 @Inject
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
            getSubjectInternAzureV2(contextHolder)
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
            getSubjectInternAzureV2(contextHolder)
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
            mapListe(alternativer, opprett2TidOgSted),
            getSubjectInternAzureV2(contextHolder)
        )

        metric.tellEndepunktKall("nye_alternativer")
    }
}

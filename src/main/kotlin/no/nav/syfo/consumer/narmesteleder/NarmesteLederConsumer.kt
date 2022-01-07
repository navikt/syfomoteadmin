package no.nav.syfo.consumer.narmesteleder

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.api.auth.OIDCIssuer
import no.nav.syfo.api.auth.OIDCUtil
import no.nav.syfo.cache.CacheConfig.Companion.CACHENAME_ISNARMESTELEDER_LEDERE
import no.nav.syfo.cache.CacheConfig.Companion.CACHENAME_ISNARMESTELEDER_LEDERRELASJONER
import no.nav.syfo.consumer.azuread.v2.AzureAdV2TokenConsumer
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.*
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Component
class NarmesteLederConsumer @Autowired constructor(
    @Value("\${isnarmesteleder.host}") private val isnarmestelederHost: String,
    @Value("\${isnarmesteleder.id}") private val isnarmestelederId: String,
    private val azureAdV2TokenConsumer: AzureAdV2TokenConsumer,
    private val metric: Metric,
    @Qualifier("restTemplateWithProxy") private val restTemplateWithProxy: RestTemplate,
    private val contextHolder: OIDCRequestContextHolder
) {
    private val isnarmestelederUrl: String
    private val isnarmestelederSystemUrl: String

    init {
        this.isnarmestelederUrl = "$isnarmestelederHost/api/v1/narmestelederrelasjon"
        this.isnarmestelederSystemUrl = "$isnarmestelederHost/api/system/v1/narmestelederrelasjoner"
    }

    fun narmesteLeder(innbyggerIdent: String, virksomhetsnummer: String): NarmesteLederRelasjonDTO? {
        val ledere = ledereForInnbygger(innbyggerIdent)

        return ledere.find { it.virksomhetsnummer == virksomhetsnummer && it.status == "INNMELDT_AKTIV" }
    }

    @Cacheable(
        value = [CACHENAME_ISNARMESTELEDER_LEDERE],
        key = "#innbyggerIdent",
        condition = "#innbyggerIdent != null"
    )
    fun ledereForInnbygger(innbyggerIdent: String): List<NarmesteLederRelasjonDTO> {
        val callId = createCallId()
        try {
            val response = restTemplateWithProxy.exchange(
                "$isnarmestelederUrl/personident",
                HttpMethod.GET,
                entityWithOboToken(innbyggerIdent, callId),
                object : ParameterizedTypeReference<List<NarmesteLederRelasjonDTO>>() {}
            )

            metric.countEvent(CALL_ISNARMESTELEDER_LEDERE_SUCCESS)

            return response.body
                ?: throw RuntimeException("Vellykket kall til isnarmesteleder, men med tom body, det skal ikke skje! callId=$callId")
        } catch (e: RestClientResponseException) {
            LOG.error("Request to get Ledere from isnarmesteleder failed with status ${e.rawStatusCode}, CallId=$callId, and message ${e.responseBodyAsString}")
            metric.countEvent(CALL_ISNARMESTELEDER_LEDERE_FAIL)
            throw e
        }
    }

    fun getLedereUsingSystemToken(innbyggerIdent: String): List<NarmesteLederRelasjonDTO> {
        val lederRelasjoner = getNarmesteLederRelasjonerUsingSystemToken(innbyggerIdent)

        return lederRelasjoner.relasjonerWhereIdentIsInnbygger(innbyggerIdent)
    }

    fun getAnsatteUsingSystemToken(lederIdent: String): List<NarmesteLederRelasjonDTO> {
        val lederRelasjoner = getNarmesteLederRelasjonerUsingSystemToken(lederIdent)

        return lederRelasjoner.relasjonerWhereIdentIsLeder(lederIdent)
    }

    @Cacheable(
        value = [CACHENAME_ISNARMESTELEDER_LEDERRELASJONER],
        key = "#ident",
        condition = "#ident != null"
    )
    fun getNarmesteLederRelasjonerUsingSystemToken(ident: String): List<NarmesteLederRelasjonDTO> {
        val callId = createCallId()
        try {
            val response = restTemplateWithProxy.exchange(
                isnarmestelederSystemUrl,
                HttpMethod.GET,
                entityAzureadV2SystemToken(ident, callId),
                object : ParameterizedTypeReference<List<NarmesteLederRelasjonDTO>>() {}
            )

            metric.countEvent(CALL_ISNARMESTELEDER_LEDERE_SUCCESS)

            return response.body
                ?: throw RuntimeException("Vellykket kall til isnarmesteleder med systemtoken, men med tom body, det skal ikke skje! callId=$callId")

        } catch (e: RestClientResponseException) {
            LOG.error("Request to get lederRelasjoner from isnarmesteleder failed with status ${e.rawStatusCode}, CallId=$callId, and message ${e.responseBodyAsString}")
            metric.countEvent(CALL_ISNARMESTELEDER_LEDERE_FAIL)
            throw e
        }
    }

    private fun entityWithOboToken(innbyggerIdent: String, callId: String): HttpEntity<*> {
        val veilederToken = OIDCUtil.tokenFraOIDC(contextHolder, OIDCIssuer.VEILEDER_AZURE_V2)
        val veilederId = OIDCUtil.getSubjectInternAzureV2(contextHolder)
        val azp = OIDCUtil.getAzpAzureV2(contextHolder)
        val oboToken = azureAdV2TokenConsumer.getOnBehalfOfToken(
            scopeClientId = isnarmestelederId,
            token = veilederToken,
            veilederId = veilederId,
            azp = azp,
        )
        return getEntity(innbyggerIdent, callId, oboToken)
    }

    private fun entityAzureadV2SystemToken(ident: String, callId: String): HttpEntity<*> {
        val token = azureAdV2TokenConsumer.getSystemToken(isnarmestelederId)
        return getEntity(ident, callId, token)
    }

    private fun getEntity(ident: String, callId: String, token: String): HttpEntity<*> {
        val headers = HttpHeaders()
        headers[HttpHeaders.AUTHORIZATION] = bearerCredentials(token)
        headers[NAV_CALL_ID_HEADER] = callId
        headers[NAV_PERSONIDENT_HEADER] = ident
        headers[NAV_CONSUMER_ID_HEADER] = APP_CONSUMER_ID

        return HttpEntity<Any>(headers)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(NarmesteLederConsumer::class.java)

        private const val CALL_ISNARMESTELEDER_LEDERE_BASE = "call_isnarmesteleder_ledere"
        private const val CALL_ISNARMESTELEDER_LEDERE_FAIL = "${CALL_ISNARMESTELEDER_LEDERE_BASE}_fail"
        private const val CALL_ISNARMESTELEDER_LEDERE_SUCCESS = "${CALL_ISNARMESTELEDER_LEDERE_BASE}_success"
    }
}

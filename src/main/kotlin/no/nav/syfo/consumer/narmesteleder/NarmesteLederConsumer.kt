package no.nav.syfo.consumer.narmesteleder

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.api.auth.OIDCIssuer
import no.nav.syfo.api.auth.OIDCUtil
import no.nav.syfo.cache.CacheConfig.Companion.CACHENAME_ISNARMESTELEDER_LEDERE
import no.nav.syfo.cache.CacheConfig.Companion.CACHENAME_NARMESTELEDER_ANSATTE
import no.nav.syfo.cache.CacheConfig.Companion.CACHENAME_NARMESTELEDER_LEDERE
import no.nav.syfo.consumer.azuread.AzureAdTokenConsumer
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
    @Value("\${syfonarmesteleder.url}") private val syfonarmestelederUrl: String,
    @Value("\${syfonarmesteleder.id}") private val syfonarmestelederId: String,
    private val azureAdTokenConsumer: AzureAdTokenConsumer,
    private val azureAdV2TokenConsumer: AzureAdV2TokenConsumer,
    private val metric: Metric,
    private val restTemplate: RestTemplate,
    @Qualifier("restTemplateWithProxy") private val restTemplateWithProxy: RestTemplate,
    private val contextHolder: OIDCRequestContextHolder
) {
    private val isnarmestelederBaseUrl: String
    private val syfonarmestelederBaseUrl: String

    init {
        this.isnarmestelederBaseUrl = "$isnarmestelederHost/api/v1/narmestelederrelasjon"
        this.syfonarmestelederBaseUrl = "$syfonarmestelederUrl/syfonarmesteleder"
    }

    fun narmesteLeder(innbyggerIdent: String, virksomhetsnummer: String): NarmesteLederRelasjonDTO? {
        val ledere = ledereForInnbygger(innbyggerIdent)

        return ledere.find { it.virksomhetsnummer == virksomhetsnummer && it.status == "INNMELDT_AKTIV" }
    }

    @Cacheable(value = [CACHENAME_ISNARMESTELEDER_LEDERE], key = "#innbyggerIdent ", condition = "#innbyggerIdent != null")
    fun ledereForInnbygger(innbyggerIdent: String): List<NarmesteLederRelasjonDTO> {
        val callId = createCallId()
        try {
            val response = restTemplateWithProxy.exchange(
                "$isnarmestelederBaseUrl/personident",
                HttpMethod.GET,
                entityWithOboToken(innbyggerIdent, callId),
                object : ParameterizedTypeReference<List<NarmesteLederRelasjonDTO>>() {}
            )

            metric.countEvent(CALL_ISNARMESTELEDER_LEDERE_SUCCESS)

            return response.body ?: throw RuntimeException("Vellykket kall til isnarmesteleder, men med tom body, det skal ikke skje! callId=$callId")
        } catch (e: RestClientResponseException) {
            LOG.error("Request to get Ledere from isnarmesteleder failed with status ${e.rawStatusCode}, CallId=$callId, and message ${e.responseBodyAsString}")
            metric.countEvent(CALL_ISNARMESTELEDER_LEDERE_FAIL)
            throw e
        }
    }

    @Cacheable(value = [CACHENAME_NARMESTELEDER_ANSATTE], key = "#aktorId", condition = "#aktorId != null")
    fun narmestelederRelasjonerAnsatte(aktorId: String): List<NarmesteLederRelasjon> {
        val callId = createCallId()
        try {
            val response: ResponseEntity<List<NarmesteLederRelasjon>> = restTemplate.exchange(
                getAnsatteUrl(aktorId),
                HttpMethod.GET,
                entity(callId),
                object : ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {}
            )

            if (response.body == null) {
                LOG.warn("Request to get Ansatte from Syfonarmesteleder was null, CallId=$callId, Response: $response")
                return emptyList()
            }

            metric.countEvent(CALL_SYFONARMESTELEDER_ANSATTE_SUCCESS)

            return response.body
        } catch (e: RestClientResponseException) {
            LOG.error("Request to get Ansatte from Syfonarmesteleder failed with status ${e.rawStatusCode}, CallId=$callId, message ${e.responseBodyAsString}")
            metric.countEvent(CALL_SYFONARMESTELEDER_ANSATTE_FAIL)
            throw e
        }
    }

    @Cacheable(value = [CACHENAME_NARMESTELEDER_LEDERE], key = "#aktorId", condition = "#aktorId != null")
    fun narmestelederRelasjonerLedere(aktorId: String): List<NarmesteLederRelasjon> {
        val callId = createCallId()
        try {
            val response = restTemplate.exchange(
                getLedereUrl(aktorId),
                HttpMethod.GET,
                entity(callId),
                object : ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {}
            )

            if (response.body == null) {
                LOG.warn("Request to get Ledere from Syfonarmesteleder was null, CallId=$callId, Response=$response")
                return emptyList()
            }

            metric.countEvent(CALL_SYFONARMESTELEDER_LEDERE_SUCCESS)

            return response.body
        } catch (e: RestClientResponseException) {
            LOG.error("Request to get Ledere from Syfonarmesteleder failed with status ${e.rawStatusCode}, CallId=$callId, message ${e.responseBodyAsString}")
            metric.countEvent(CALL_SYFONARMESTELEDER_LEDERE_FAIL)
            throw e
        }
    }

    private fun entity(callId: String): HttpEntity<*> {
        val token = azureAdTokenConsumer.accessToken(syfonarmestelederId)
        val headers = HttpHeaders()
        headers[HttpHeaders.AUTHORIZATION] = bearerCredentials(token)
        headers[NAV_CALL_ID_HEADER] = createCallId()
        headers[SYFONARMESTELEDER_CALL_ID_HEADER] = createCallId()
        headers[NAV_CONSUMER_ID_HEADER] = APP_CONSUMER_ID
        return HttpEntity<Any>(headers)
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
        val headers = HttpHeaders()
        headers[HttpHeaders.AUTHORIZATION] = bearerCredentials(oboToken)
        headers[NAV_CALL_ID_HEADER] = callId
        headers[SYFONARMESTELEDER_CALL_ID_HEADER] = callId
        headers[NAV_PERSONIDENT_HEADER] = innbyggerIdent
        headers[NAV_CONSUMER_ID_HEADER] = APP_CONSUMER_ID
        return HttpEntity<Any>(headers)
    }

    private fun getAnsatteUrl(aktorId: String): String {
        return "$syfonarmestelederBaseUrl/narmesteLeder/$aktorId"
    }

    private fun getLedereUrl(aktorId: String): String {
        return "$syfonarmestelederBaseUrl/sykmeldt/$aktorId/narmesteledere"
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(NarmesteLederConsumer::class.java)

        private const val CALL_SYFONARMESTELEDER_ANSATTE_BASE = "call_syfonarmesteleder_ansatte"
        private const val CALL_SYFONARMESTELEDER_ANSATTE_FAIL = "${CALL_SYFONARMESTELEDER_ANSATTE_BASE}_fail"
        private const val CALL_SYFONARMESTELEDER_ANSATTE_SUCCESS = "${CALL_SYFONARMESTELEDER_ANSATTE_BASE}_success"

        private const val CALL_ISNARMESTELEDER_LEDERE_BASE = "call_isnarmesteleder_ledere"
        private const val CALL_ISNARMESTELEDER_LEDERE_FAIL = "${CALL_ISNARMESTELEDER_LEDERE_BASE}_fail"
        private const val CALL_ISNARMESTELEDER_LEDERE_SUCCESS = "${CALL_ISNARMESTELEDER_LEDERE_BASE}_success"

        private const val CALL_SYFONARMESTELEDER_LEDERE_BASE = "call_syfonarmesteledere_leder"
        private const val CALL_SYFONARMESTELEDER_LEDERE_FAIL = "${CALL_SYFONARMESTELEDER_LEDERE_BASE}_fail"
        private const val CALL_SYFONARMESTELEDER_LEDERE_SUCCESS = "${CALL_SYFONARMESTELEDER_LEDERE_BASE}_success"

        private const val SYFONARMESTELEDER_CALL_ID_HEADER = "Nav-Callid"
    }
}

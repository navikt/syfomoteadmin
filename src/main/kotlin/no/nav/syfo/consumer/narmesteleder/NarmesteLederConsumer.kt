package no.nav.syfo.consumer.narmesteleder

import no.nav.syfo.cache.CacheConfig.Companion.CACHENAME_NARMESTELEDER_ANSATTE
import no.nav.syfo.cache.CacheConfig.Companion.CACHENAME_NARMESTELEDER_LEDER
import no.nav.syfo.cache.CacheConfig.Companion.CACHENAME_NARMESTELEDER_LEDERE
import no.nav.syfo.consumer.azuread.AzureAdTokenConsumer
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class NarmesteLederConsumer @Autowired constructor(
    @Value("\${syfonarmesteleder.url}") private val syfonarmestelederUrl: String,
    private val azureAdTokenConsumer: AzureAdTokenConsumer,
    private val metric: Metric,
    private val restTemplate: RestTemplate,
    @Value("\${syfonarmesteleder.id}") private val syfonarmestelederId: String
) {
    private val syfonarmestelederBaseUrl: String

    init {
        this.syfonarmestelederBaseUrl = "$syfonarmestelederUrl/syfonarmesteleder"
    }

    @Cacheable(value = [CACHENAME_NARMESTELEDER_LEDER], key = "#aktorId + #virksomhetsnummer", condition = "#aktorId != null && #virksomhetsnummer != null")
    fun narmesteLederRelasjonLeder(aktorId: String, virksomhetsnummer: String): NarmesteLederRelasjon? {
        val callId = createCallId()
        try {
            val response = restTemplate.exchange(
                getLederUrl(aktorId, virksomhetsnummer),
                HttpMethod.GET,
                entity(callId),
                NarmestelederResponse::class.java
            )
            metric.countEvent(CALL_SYFONARMESTELEDER_LEDER_SUCCESS)

            return response.body.narmesteLederRelasjon
        } catch (e: RestClientResponseException) {
            LOG.error("Request to get Leder from Syfonarmesteleder failed with status ${e.rawStatusCode}, CallId=$callId, and message ${e.responseBodyAsString}")
            metric.countEvent(CALL_SYFONARMESTELEDER_LEDER_FAIL)
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

    private fun getAnsatteUrl(aktorId: String): String {
        return "$syfonarmestelederBaseUrl/narmesteLeder/$aktorId"
    }

    private fun getLederUrl(aktorId: String, virksomhetsnummer: String): String {
        return UriComponentsBuilder
            .fromHttpUrl("$syfonarmestelederBaseUrl/sykmeldt/$aktorId")
            .queryParam("orgnummer", virksomhetsnummer)
            .toUriString()
    }

    private fun getLedereUrl(aktorId: String): String {
        return "$syfonarmestelederBaseUrl/sykmeldt/$aktorId/narmesteledere"
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(NarmesteLederConsumer::class.java)

        private const val CALL_SYFONARMESTELEDER_ANSATTE_BASE = "call_syfonarmesteleder_ansatte"
        private const val CALL_SYFONARMESTELEDER_ANSATTE_FAIL = "${CALL_SYFONARMESTELEDER_ANSATTE_BASE}_fail"
        private const val CALL_SYFONARMESTELEDER_ANSATTE_SUCCESS = "${CALL_SYFONARMESTELEDER_ANSATTE_BASE}_success"

        private const val CALL_SYFONARMESTELEDER_LEDER_BASE = "call_syfonarmesteleder_leder"
        private const val CALL_SYFONARMESTELEDER_LEDER_FAIL = "${CALL_SYFONARMESTELEDER_LEDER_BASE}_fail"
        private const val CALL_SYFONARMESTELEDER_LEDER_SUCCESS = "${CALL_SYFONARMESTELEDER_LEDER_BASE}_success"

        private const val CALL_SYFONARMESTELEDER_LEDERE_BASE = "call_syfonarmesteledere_leder"
        private const val CALL_SYFONARMESTELEDER_LEDERE_FAIL = "${CALL_SYFONARMESTELEDER_LEDERE_BASE}_fail"
        private const val CALL_SYFONARMESTELEDER_LEDERE_SUCCESS = "${CALL_SYFONARMESTELEDER_LEDERE_BASE}_success"

        private const val SYFONARMESTELEDER_CALL_ID_HEADER = "Nav-Callid"
    }
}

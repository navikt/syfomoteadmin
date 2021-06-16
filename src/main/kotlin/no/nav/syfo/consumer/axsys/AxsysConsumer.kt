package no.nav.syfo.consumer.axsys

import no.nav.syfo.cache.CacheConfig
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import javax.inject.Inject

@Service
class AxsysConsumer @Inject constructor(
    @Value("\${axsys.url}") private val axsysUrl: String,
    private val metric: Metric,
    private val restTemplate: RestTemplate
) {
    private val axsysTilgangerUrl: String

    init {
        this.axsysTilgangerUrl = "$axsysUrl${AXSYS_TILGANGER_PATH}"
    }

    fun axsysTilgangerResponse(navIdent: String): AxsysTilgangerResponse {
        try {
            val response = restTemplate.exchange(
                    getAxsysTilgangUrl(navIdent),
                    HttpMethod.GET,
                    entity(),
                    AxsysTilgangerResponse::class.java
            )
            val axsysResponse = response.body!!
            metric.countEvent(METRIC_CALL_AXSYS_SUCCESS)
            return axsysResponse
        } catch (e: RestClientResponseException) {
            metric.countEvent(METRIC_CALL_AXSYS_FAIL)
            val message = "Call to get Tilganger from Axsys failed with status: ${e.rawStatusCode} and message: ${e.responseBodyAsString}"
            LOG.error(message)
            throw e
        }
    }

    @Cacheable(value = [CacheConfig.CACHENAME_AXSYS_ENHETER], key = "#navIdent", condition = "#navIdent != null")
    fun enheter(navIdent: String): List<AxsysEnhet> {
        return axsysTilgangerResponse(navIdent).enheter
    }

    private fun getAxsysTilgangUrl(navIdent: String): String {
        return "$axsysTilgangerUrl/$navIdent"
    }

    private fun entity(): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers[NAV_CONSUMER_ID_HEADER] = APP_CONSUMER_ID
        headers[NAV_CALL_ID_HEADER] = createCallId()
        return HttpEntity(headers)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AxsysConsumer::class.java)

        private const val METRIC_CALL_AXSYS_SUCCESS = "call_axsys_success"
        private const val METRIC_CALL_AXSYS_FAIL = "call_axsys_fail"
        private const val AXSYS_TILGANGER_PATH = "/api/v1/tilgang"
    }
}

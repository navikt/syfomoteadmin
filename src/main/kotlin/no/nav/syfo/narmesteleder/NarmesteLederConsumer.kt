package no.nav.syfo.narmesteleder

import no.nav.syfo.azuread.AzureAdTokenConsumer
import no.nav.syfo.config.CacheConfig.CACHENAME_NARMESTELEDER_ANSATTE
import no.nav.syfo.config.CacheConfig.CACHENAME_NARMESTELEDER_LEDER
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class NarmesteLederConsumer @Autowired constructor(
        private val azureAdTokenConsumer: AzureAdTokenConsumer,
        private val metrikk: Metrikk,
        private val restTemplate: RestTemplate,
        @param:Value("\${syfonarmesteleder.id}") private val syfonarmestelederId: String
) {
    @Cacheable(value = [CACHENAME_NARMESTELEDER_LEDER], key = "#aktorId + #virksomhetsnummer", condition = "#aktorId != null && #virksomhetsnummer != null")
    fun narmesteLederRelasjonLeder(aktorId: String, virksomhetsnummer: String): NarmesteLederRelasjon? {
        try {
            val response = restTemplate.exchange<NarmestelederResponse>(
                    getLederUrl(aktorId, virksomhetsnummer),
                    HttpMethod.GET,
                    entity(),
                    NarmestelederResponse::class.java
            )
            metrikk.countEvent(CALL_SYFONARMESTELEDER_LEDER_SUCCESS)

            return response.body!!.narmesteLederRelasjon
        } catch (e: RestClientResponseException) {
            LOG.error("Request to get Leder from Syfonarmesteleder failed with status ${e.rawStatusCode} and message: ${e.responseBodyAsString}" )
            metrikk.countEvent(CALL_SYFONARMESTELEDER_LEDER_FAIL)
            throw e
        }
    }

    @Cacheable(value = [CACHENAME_NARMESTELEDER_ANSATTE], key = "#aktorId", condition = "#aktorId != null")
    fun narmestelederRelasjonerAnsatte(aktorId: String): List<NarmesteLederRelasjon> {
        try {
            val response = restTemplate.exchange<List<NarmesteLederRelasjon>>(
                    getAnsatteUrl(aktorId),
                    HttpMethod.GET,
                    entity(),
                    object : ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {}
            )
            metrikk.countEvent(CALL_SYFONARMESTELEDER_ANSATTE_SUCCESS)

            return response.body!!
        } catch (e: RestClientResponseException) {
            LOG.error("Request to get Ansatte from Syfonarmesteleder failed with status ${e.rawStatusCode} and message: ${e.responseBodyAsString}" )
            metrikk.countEvent(CALL_SYFONARMESTELEDER_ANSATTE_FAIL)
            throw e
        }
    }

    private fun entity(): HttpEntity<*> {
        val token = azureAdTokenConsumer.accessToken(syfonarmestelederId)
        val headers = HttpHeaders()
        headers[HttpHeaders.AUTHORIZATION] = bearerCredentials(token)
        headers[NAV_CALL_ID_HEADER] = createCallId()
        headers[NAV_CONSUMER_ID_HEADER] = APP_CONSUMER_ID
        return HttpEntity<Any>(headers)
    }

    private fun getAnsatteUrl(aktorId: String): String {
        return "$SYFONARMESTELEDER_BASEURL/narmesteLeder/$aktorId"
    }

    private fun getLederUrl(aktorId: String, virksomhetsnummer: String): String {
        return UriComponentsBuilder
                .fromHttpUrl("$SYFONARMESTELEDER_BASEURL/sykmeldt/$aktorId")
                .queryParam("orgnummer", virksomhetsnummer)
                .toUriString()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(NarmesteLederConsumer::class.java)
        private const val SYFONARMESTELEDER_BASEURL = "http://syfonarmesteleder/syfonarmesteleder"

        private const val CALL_SYFONARMESTELEDER_ANSATTE_BASE = "call_syfonarmesteleder_ansatte"
        private const val CALL_SYFONARMESTELEDER_ANSATTE_FAIL = "${CALL_SYFONARMESTELEDER_ANSATTE_BASE}_fail"
        private const val CALL_SYFONARMESTELEDER_ANSATTE_SUCCESS = "${CALL_SYFONARMESTELEDER_ANSATTE_BASE}_success"

        private const val CALL_SYFONARMESTELEDER_LEDER_BASE = "call_syfonarmesteleder_leder"
        private const val CALL_SYFONARMESTELEDER_LEDER_FAIL = "${CALL_SYFONARMESTELEDER_LEDER_BASE}_fail"
        private const val CALL_SYFONARMESTELEDER_LEDER_SUCCESS = "${CALL_SYFONARMESTELEDER_LEDER_BASE}_success"
    }
}

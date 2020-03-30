package no.nav.syfo.narmesteleder

import no.nav.syfo.azuread.AzureAdTokenConsumer
import no.nav.syfo.config.CacheConfig.CACHENAME_NARMESTELEDER_ANSATTE
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.util.NAV_CALL_ID_HEADER
import no.nav.syfo.util.bearerCredentials
import no.nav.syfo.util.createCallId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Component
class NarmesteLederConsumer @Autowired constructor(
        private val azureAdTokenConsumer: AzureAdTokenConsumer,
        private val metrikk: Metrikk,
        private val restTemplate: RestTemplate,
        @param:Value("\${syfonarmesteleder.id}") private val syfonarmestelederId: String
) {
    fun narmestelederRelasjoner(aktorId: String): List<NarmesteLederRelasjon> {
        try {
            val response: ResponseEntity<List<NarmesteLederRelasjon>> = restTemplate.exchange<List<NarmesteLederRelasjon>>(
                    getAnsatteUrl(aktorId),
                    HttpMethod.GET,
                    entity(),
                    object : ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {}
            )
            metrikk.countEvent(CALL_SYFONARMESTELEDER_ANSATTE_SUCCESS)

            return response.body!!
        } catch (e: RestClientResponseException) {
            LOG.error("Request to get Ansatte from Syfonarmesteleder failed with status " + e.rawStatusCode + " and message: " + e.responseBodyAsString)
            metrikk.countEvent(CALL_SYFONARMESTELEDER_ANSATTE_FAIL)
            throw e
        }
    }

    @Cacheable(value = [CACHENAME_NARMESTELEDER_ANSATTE], key = "#aktorId", condition = "#aktorId != null")
    fun ansatte(aktorId: String): List<Ansatt> {
        return narmestelederRelasjoner(aktorId).map {
            Ansatt(
                    aktoerId = it.aktorId,
                    virksomhetsnummer = it.orgnummer
            )
        }
    }

    private fun entity(): HttpEntity<*> {
        val token = azureAdTokenConsumer.accessToken(syfonarmestelederId)
        val headers = HttpHeaders()
        headers[HttpHeaders.AUTHORIZATION] = bearerCredentials(token)
        headers[NAV_CALL_ID_HEADER] = createCallId()
        return HttpEntity<Any>(headers)
    }

    private fun getAnsatteUrl(aktorId: String): String {
        return "http://syfonarmesteleder/syfonarmesteleder/narmesteLeder/$aktorId"
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(NarmesteLederConsumer::class.java)
        private const val CALL_SYFONARMESTELEDER_ANSATTE_BASE = "call_syfonarmesteleder_ansatte"
        private const val CALL_SYFONARMESTELEDER_ANSATTE_FAIL = "${CALL_SYFONARMESTELEDER_ANSATTE_BASE}_fail"
        private const val CALL_SYFONARMESTELEDER_ANSATTE_SUCCESS = "${CALL_SYFONARMESTELEDER_ANSATTE_BASE}_success"
    }
}
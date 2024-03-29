package no.nav.syfo.consumer.dkif

import no.nav.syfo.cache.CacheConfig
import no.nav.syfo.metric.Metric
import no.nav.syfo.consumer.sts.StsConsumer
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Service
class DkifConsumer(
    @Value("\${dkif.url}") private val dkifUrl: String,
    private val metric: Metric,
    private val stsConsumer: StsConsumer,
    private val template: RestTemplate
) {
    private val dkifKontaktinfoUrl: String

    init {
        this.dkifKontaktinfoUrl = "$dkifUrl$DKIF_KONTAKTINFO_PATH"
    }

    @Cacheable(cacheNames = [CacheConfig.CACHENAME_DKIF_IDENT], key = "#ident", condition = "#ident != null")
    fun kontaktinformasjon(ident: String): DigitalKontaktinfo {
        val bearer = stsConsumer.token()

        try {
            val response = template.exchange(
                dkifKontaktinfoUrl,
                HttpMethod.GET,
                entity(ident, bearer),
                DigitalKontaktinfoBolk::class.java
            )
            val responseBody = response.body

            if (responseBody != null) {
                val kontaktinfo = responseBody.kontaktinfo?.get(ident)
                val feil = responseBody.feil?.get(ident)
                when {
                    kontaktinfo != null -> {
                        metric.countOutgoingReponses(METRIC_CALL_DKIF, response.statusCodeValue)
                        return kontaktinfo
                    }
                    feil != null -> {
                        if (feil.melding == "Ingen kontaktinformasjon er registrert på personen") {
                            return DigitalKontaktinfo(
                                kanVarsles = false,
                                personident = ident
                            )
                        } else {
                            throw DKIFRequestFailedException(feil.melding)
                        }
                    }
                    else -> {
                        throw DKIFRequestFailedException("Kontaktinfo is null")
                    }
                }
            } else {
                throw DKIFRequestFailedException("ReponseBody is null")
            }
        } catch (e: DKIFRequestFailedException) {
            LOG.error("Failed to get Kontaktinfo from DKIF with error: ${e.message}")
            throw e
        } catch (e: RestClientResponseException) {
            LOG.error("Request to get Kontaktinfo from DKIF failed with HTTP-status: ${e.rawStatusCode} and ${e.statusText}")
            metric.countOutgoingReponses(METRIC_CALL_DKIF, e.rawStatusCode)
            throw e
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DkifConsumer::class.java)

        const val METRIC_CALL_DKIF = "call_dkif"
        const val DKIF_KONTAKTINFO_PATH = "/api/v1/personer/kontaktinformasjon"
    }

    private fun entity(ident: String, token: String): HttpEntity<String> {
        val headers = HttpHeaders()

        headers.contentType = MediaType.APPLICATION_JSON
        headers[HttpHeaders.AUTHORIZATION] = bearerCredentials(token)
        headers[NAV_CONSUMER_ID_HEADER] = APP_CONSUMER_ID
        headers[NAV_CALL_ID_HEADER] = createCallId()
        headers[NAV_PERSONIDENTER_HEADER] = ident

        return HttpEntity(headers)
    }
}

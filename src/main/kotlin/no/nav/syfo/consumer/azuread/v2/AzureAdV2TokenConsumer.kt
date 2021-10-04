package no.nav.syfo.consumer.azuread.v2

import no.nav.syfo.metric.Metric
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.*
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import java.util.concurrent.ConcurrentHashMap

@Component
class AzureAdV2TokenConsumer @Autowired constructor(
    @Qualifier("restTemplateWithProxy") private val restTemplateWithProxy: RestTemplate,
    @Value("\${azure.app.client.id}") private val azureAppClientId: String,
    @Value("\${azure.app.client.secret}") private val azureAppClientSecret: String,
    @Value("\${azure.openid.config.token.endpoint}") private val azureTokenEndpoint: String,
    private val metric: Metric,
) {
    fun getOnBehalfOfToken(
        scopeClientId: String,
        token: String,
        veilederId: String,
        azp: String,
    ): String {
        val cacheKey = "$veilederId-$azp-$scopeClientId"
        val cachedToken = tokenCache[cacheKey]
        return if (cachedToken == null || cachedToken.isExpired()) {
            try {
                val response = restTemplateWithProxy.exchange(
                    azureTokenEndpoint,
                    HttpMethod.POST,
                    requestEntity(scopeClientId, token),
                    AzureAdV2TokenResponse::class.java
                )
                val tokenResponse = response.body!!
                val azureToken = tokenResponse.toAzureAdV2Token()
                tokenCache[cacheKey] = azureToken
                metric.countEvent(CALL_AZUREAD_CACHE_MISS)
                azureToken.accessToken
            } catch (e: RestClientResponseException) {
                log.error(
                    "Call to get AzureADV2Token from AzureAD for scope: $scopeClientId with status: ${e.rawStatusCode} and message: ${e.responseBodyAsString}",
                    e
                )
                throw e
            }
        } else {
            metric.countEvent(CALL_AZUREAD_CACHE_HIT)
            cachedToken.accessToken
        }

    }

    private fun requestEntity(
        scopeClientId: String,
        token: String
    ): HttpEntity<MultiValueMap<String, String>> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("client_id", azureAppClientId)
        body.add("client_secret", azureAppClientSecret)
        body.add("client_assertion_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
        body.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
        body.add("assertion", token)
        body.add("scope", "api://$scopeClientId/.default")
        body.add("requested_token_use", "on_behalf_of")
        return HttpEntity<MultiValueMap<String, String>>(body, headers)
    }

    companion object {
        private val tokenCache = ConcurrentHashMap<String, AzureAdV2Token>()
        private val log = LoggerFactory.getLogger(AzureAdV2TokenConsumer::class.java)
        private val CALL_AZUREAD_BASE = "call_azuread"
        private val CALL_AZUREAD_CACHE_HIT = "${CALL_AZUREAD_BASE}_cache_hit"
        private val CALL_AZUREAD_CACHE_MISS = "${CALL_AZUREAD_BASE}_cache_miss"
    }
}

package no.nav.syfo.consumer.veiledertilgang

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.api.auth.OIDCIssuer
import no.nav.syfo.api.auth.OIDCUtil
import no.nav.syfo.consumer.azuread.v2.AzureAdV2TokenConsumer
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.*
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import javax.ws.rs.ForbiddenException

@Service
class VeilederTilgangConsumer(
    @Value("\${tilgangskontrollapi.url}") private val tilgangskontrollUrl: String,
    @Value("\${syfotilgangskontroll.client.id}") private val syfotilgangskontrollClientId: String,
    private val azureAdV2TokenConsumer: AzureAdV2TokenConsumer,
    private val metric: Metric,
    private val template: RestTemplate,
    private val contextHolder: OIDCRequestContextHolder
) {
    private val tilgangTilBrukerViaAzureUriTemplate: UriComponentsBuilder

    fun throwExceptionIfDeniedAccessAzureOBO(fnr: Fodselsnummer) {
        val hasAccess = hasVeilederAccessToPersonWithAzureOBO(fnr)
        if (!hasAccess) {
            throw ForbiddenException()
        }
    }

    fun hasVeilederAccessToPersonWithAzureOBO(fnr: Fodselsnummer): Boolean {
        try {
            val token = OIDCUtil.tokenFraOIDC(contextHolder, OIDCIssuer.VEILEDER_AZURE_V2)
            val oboToken = azureAdV2TokenConsumer.getOnBehalfOfToken(
                scopeClientId = syfotilgangskontrollClientId,
                token = token
            )

            val response = template.exchange(
                accessToUserV2Url(fnr),
                HttpMethod.GET,
                entity(token = oboToken),
                String::class.java
            )
            return response.statusCode.is2xxSuccessful
        } catch (e: HttpClientErrorException) {
            return if (e.rawStatusCode == 403) {
                metric.countEvent(METRIC_CALL_VEILEDERTILGANG_V2_USER_DENIED)
                false
            } else {
                metric.countEvent(METRIC_CALL_VEILEDERTILGANG_V2_USER_FAIL)
                LOG.error("Error requesting access to person from Syfo-tilgangskontroll with status-${e.rawStatusCode}: ", e)
                throw e
            }
        } catch (e: HttpServerErrorException) {
            metric.countEvent(METRIC_CALL_VEILEDERTILGANG_V2_USER_FAIL)
            LOG.error("Error requesting access to person from Syfo-tilgangskontroll with status-${e.rawStatusCode}: ", e)
            throw e
        }
    }

    fun accessToUserV2Url(fnr: Fodselsnummer): String {
        return "$tilgangskontrollUrl$ACCESS_TO_USER_WITH_AZURE_V2_PATH/${fnr.value}"
    }

    fun throwExceptionIfVeilederWithoutAccess(fnr: Fodselsnummer) {
        val harTilgang = hasVeilederAccessToPerson(fnr.value)
        if (!harTilgang) {
            throw ForbiddenException()
        }
    }

    fun hasVeilederAccessToPerson(fnr: String): Boolean {
        val httpEntity = entity(
            token = OIDCUtil.tokenFraOIDC(contextHolder, OIDCIssuer.AZURE)
        )
        return try {
            val uri = tilgangTilBrukerViaAzureUriTemplate.build(Collections.singletonMap(FNR, fnr))
            template.exchange(
                uri,
                HttpMethod.GET,
                httpEntity,
                String::class.java
            )
            true
        } catch (e: HttpClientErrorException) {
            if (e.rawStatusCode == 403) {
                false
            } else {
                metric.countEvent(METRIC_CALL_VEILEDERTILGANG_USER_FAIL)
                LOG.error("Error requesting access to peson from Syfo-tilgangskontroll with status-${e.rawStatusCode} callId-${httpEntity.headers[NAV_CALL_ID_HEADER]}: ", e)
                throw e
            }
        }
    }

    private fun entity(token: String): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers[HttpHeaders.AUTHORIZATION] = bearerCredentials(token)
        headers[NAV_CALL_ID_HEADER] = createCallId()
        headers[NAV_CONSUMER_ID_HEADER] = APP_CONSUMER_ID
        return HttpEntity(headers)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(VeilederTilgangConsumer::class.java)

        private const val METRIC_CALL_VEILEDERTILGANG_BASE = "call_syfotilgangskontroll"
        private const val METRIC_CALL_VEILEDERTILGANG_USER_FAIL = "${METRIC_CALL_VEILEDERTILGANG_BASE}_user_fail"
        private const val METRIC_CALL_VEILEDERTILGANG_V2_USER_DENIED = "${METRIC_CALL_VEILEDERTILGANG_BASE}_user_v2_denied"
        private const val METRIC_CALL_VEILEDERTILGANG_V2_USER_FAIL = "${METRIC_CALL_VEILEDERTILGANG_BASE}_user_v2_fail"

        const val FNR = "fnr"
        const val TILGANG_TIL_BRUKER_VIA_AZURE_PATH = "/bruker"
        private const val FNR_PLACEHOLDER = "{$FNR}"

        const val ACCESS_TO_USER_WITH_AZURE_V2_PATH = "/navident/bruker"
    }

    init {
        tilgangTilBrukerViaAzureUriTemplate = UriComponentsBuilder.fromHttpUrl(tilgangskontrollUrl)
            .path(TILGANG_TIL_BRUKER_VIA_AZURE_PATH)
            .queryParam(FNR, FNR_PLACEHOLDER)
    }
}

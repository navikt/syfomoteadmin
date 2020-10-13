package no.nav.syfo.consumer.pdl

import no.nav.syfo.metric.Metric
import no.nav.syfo.consumer.sts.StsConsumer
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Service
class PdlConsumer(
    private val metric: Metric,
    @Value("\${pdl.url}") private val pdlUrl: String,
    private val stsConsumer: StsConsumer,
    private val restTemplate: RestTemplate
) {
    fun person(ident: String): PdlHentPerson? {
        val query = getPdlQuery("/pdl/hentPerson.graphql")
        val request = PdlRequest(
            query = query,
            variables = Variables(ident)
        )
        val entity = HttpEntity(
            request,
            createRequestHeaders()
        )
        try {
            val pdlPerson = restTemplate.exchange(
                    pdlUrl,
                    HttpMethod.POST,
                    entity,
                    PdlPersonResponse::class.java
            )
            val pdlPersonReponse = pdlPerson.body!!
            return if (pdlPersonReponse.errors != null && pdlPersonReponse.errors.isNotEmpty()) {
                metric.countEvent(CALL_PDL_PERSON_FAIL)
                pdlPersonReponse.errors.forEach {
                    LOG.error("Error while requesting person from PersonDataLosningen: ${it.errorMessage()}")
                }
                null
            } else {
                metric.countEvent(CALL_PDL_PERSON_SUCCESS)
                pdlPersonReponse.data
            }
        } catch (exception: RestClientResponseException) {
            metric.countEvent(CALL_PDL_PERSON_FAIL)
            LOG.error("Error from PDL with request-url: $pdlUrl", exception)
            throw exception
        }
    }

    private fun getPdlQuery(queryFilePath: String): String {
        return this::class.java.getResource(queryFilePath)
            .readText()
            .replace("[\n\r]", "")
    }

    fun fullName(ident: String): String {
        return person(ident)?.fullName() ?: throw PdlRequestFailedException()
    }

    fun isKode6(ident: String): Boolean {
        return person(ident)?.isKode6() ?: throw PdlRequestFailedException()
    }

    fun isKode6Or7(ident: String): Boolean {
        return person(ident)?.isKode6Or7() ?: throw PdlRequestFailedException()
    }

    private fun createRequestHeaders(): HttpHeaders {
        val stsToken: String = stsConsumer.token()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set(TEMA_HEADER, ALLE_TEMA_HEADERVERDI)
        headers.set(AUTHORIZATION, bearerCredentials(stsToken))
        headers.set(NAV_CONSUMER_TOKEN_HEADER, bearerCredentials(stsToken))
        return headers
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(PdlConsumer::class.java)

        private const val CALL_PDL_BASE = "call_pdl"
        const val CALL_PDL_PERSON_FAIL = "${CALL_PDL_BASE}_person_fail"
        const val CALL_PDL_PERSON_SUCCESS = "${CALL_PDL_BASE}_person_success"
    }
}

package no.nav.syfo.controller.internad

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.LocalApplication
import no.nav.syfo.consumer.veiledertilgang.VeilederTilgangConsumer
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.generateAzureAdV2TokenResponse
import no.nav.syfo.testhelper.mockAndExpectAzureADV2
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.*
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import javax.inject.Inject

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
abstract class AbstractRessursTilgangTest {

    @Value("\${azure.openid.config.token.endpoint}")
    private lateinit var azureTokenEndpoint: String

    @Value("\${tilgangskontrollapi.url}")
    private lateinit var tilgangskontrollUrl: String

    @Value("\${dev}")
    private lateinit var dev: String

    @Inject
    lateinit var oidcRequestContextHolder: OIDCRequestContextHolder

    @Inject
    private lateinit var restTemplate: RestTemplate
    private lateinit var mockRestServiceServer: MockRestServiceServer

    @Inject
    @Qualifier("restTemplateWithProxy")
    private lateinit var restTemplateWithProxy: RestTemplate
    private lateinit var mockRestServiceWithProxyServer: MockRestServiceServer

    @Before
    fun setUp() {
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
        mockRestServiceWithProxyServer = MockRestServiceServer.bindTo(restTemplateWithProxy).build()
    }

    @After
    open fun tearDown() {
        mockRestServiceServer.verify()
        loggUtAlle(oidcRequestContextHolder)
        mockRestServiceServer.reset()
        mockRestServiceWithProxyServer.reset()
    }

    val oboToken = generateAzureAdV2TokenResponse().access_token

    fun mockSvarFraTilgangTilBrukerViaAzureV2(fnr: String, status: HttpStatus) {
        mockAndExpectAzureADV2(mockRestServiceWithProxyServer, azureTokenEndpoint, generateAzureAdV2TokenResponse())

        val uriString = UriComponentsBuilder.fromHttpUrl(tilgangskontrollUrl)
            .path(VeilederTilgangConsumer.TILGANGSKONTROLL_PERSON_PATH)
            .toUriString()
        mockRestServiceServer.expect(ExpectedCount.manyTimes(), MockRestRequestMatchers.requestTo(uriString))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.header(HttpHeaders.AUTHORIZATION, "Bearer $oboToken"))
            .andExpect(MockRestRequestMatchers.header(NAV_PERSONIDENT_HEADER, fnr))
            .andRespond(MockRestResponseCreators.withStatus(status))
    }
}

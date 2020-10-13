package no.nav.syfo.controller.internad

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.LocalApplication
import no.nav.syfo.api.auth.OIDCIssuer
import no.nav.syfo.consumer.veiledertilgang.VeilederTilgangConsumer
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
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
    @Value("\${tilgangskontrollapi.url}")
    private lateinit var tilgangskontrollUrl: String

    @Value("\${dev}")
    private lateinit var dev: String

    @Inject
    lateinit var oidcRequestContextHolder: OIDCRequestContextHolder

    @Inject
    private lateinit var restTemplate: RestTemplate
    private lateinit var mockRestServiceServer: MockRestServiceServer

    @Before
    fun setUp() {
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
    }

    @After
    open fun tearDown() {
        mockRestServiceServer.verify()
        loggUtAlle(oidcRequestContextHolder)
    }

    fun mockSvarFraTilgangTilBrukerViaAzure(fnr: String?, status: HttpStatus?) {
        val uriString = UriComponentsBuilder.fromHttpUrl(tilgangskontrollUrl)
            .path(VeilederTilgangConsumer.TILGANG_TIL_BRUKER_VIA_AZURE_PATH)
            .queryParam(VeilederTilgangConsumer.FNR, fnr)
            .toUriString()
        val idToken = oidcRequestContextHolder.oidcValidationContext.getToken(OIDCIssuer.AZURE).idToken
        mockRestServiceServer.expect(ExpectedCount.manyTimes(), MockRestRequestMatchers.requestTo(uriString))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.header(HttpHeaders.AUTHORIZATION, "Bearer $idToken"))
            .andRespond(MockRestResponseCreators.withStatus(status))
    }
}
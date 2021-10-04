package no.nav.syfo.controller.internad.v2

import no.nav.syfo.LocalApplication
import no.nav.syfo.consumer.azuread.v2.AzureAdV2TokenConsumer
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.controller.internad.AbstractRessursTilgangTest
import no.nav.syfo.controller.internad.aktor.v2.AktorControllerV2
import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.testhelper.OidcTestHelper.loggInnVeilederAzureV2
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.VEILEDER_ID
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.text.ParseException
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class AktorControllerV2TilgangTest : AbstractRessursTilgangTest() {
    @Inject
    private lateinit var aktorController: AktorControllerV2

    @MockBean
    private lateinit var azureAdV2TokenConsumer: AzureAdV2TokenConsumer

    @MockBean
    private lateinit var pdlConsumer: PdlConsumer

    @Before
    fun setup() {
        Mockito.`when`(azureAdV2TokenConsumer.getOnBehalfOfToken(
            scopeClientId = anyString(),
            token = anyString(),
            veilederId = anyString(),
            azp = anyString(),
        )).thenReturn(oboToken)
        Mockito.`when`(pdlConsumer.fodselsnummer(AktorId(ARBEIDSTAKER_AKTORID))).thenReturn(Fodselsnummer(ARBEIDSTAKER_FNR))
        try {
            loggInnVeilederAzureV2(oidcRequestContextHolder, VEILEDER_ID)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    @Test
    fun hasAccess() {
        mockSvarFraTilgangTilBrukerViaAzureV2(ARBEIDSTAKER_FNR, HttpStatus.OK)
        val aktor = aktorController.get(ARBEIDSTAKER_AKTORID)
        Assert.assertEquals(ARBEIDSTAKER_FNR, aktor.fnr)
    }

    @Test(expected = ForbiddenException::class)
    fun noAccess() {
        mockSvarFraTilgangTilBrukerViaAzureV2(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN)
        aktorController.get(ARBEIDSTAKER_AKTORID)
    }
}

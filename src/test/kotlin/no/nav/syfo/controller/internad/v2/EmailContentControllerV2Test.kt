package no.nav.syfo.controller.internad.v2

import no.nav.syfo.LocalApplication
import no.nav.syfo.api.ressurser.azuread.v2.EmailContentControllerV2
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.controller.internad.AbstractRessursTilgangTest
import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.service.MoteService
import no.nav.syfo.testhelper.MoteGenerator
import no.nav.syfo.testhelper.OidcTestHelper
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.VEILEDER_ID
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.text.ParseException
import java.util.*
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class EmailContentControllerV2Test : AbstractRessursTilgangTest() {

    @MockBean
    private lateinit var moteService: MoteService

    @MockBean
    private lateinit var pdlConsumer: PdlConsumer

    @Inject
    private lateinit var emailContentController: EmailContentControllerV2

    private val moteGenerator = MoteGenerator()
    private val uuid = UUID.randomUUID()
    private val mote = moteGenerator.generateMote(uuid)

    @Before
    fun setup() {
        Mockito.`when`(pdlConsumer.fodselsnummer(AktorId(ARBEIDSTAKER_AKTORID))).thenReturn(Fodselsnummer(ARBEIDSTAKER_FNR))
        try {
            OidcTestHelper.loggInnVeilederAzureV2(oidcRequestContextHolder, VEILEDER_ID)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        Mockito.`when`(moteService.findMoteByMotedeltakerUuid(uuid.toString())).thenReturn(mote)
    }

    @Test
    fun emailContentHasAccess() {
        mockSvarFraTilgangTilBrukerViaAzureV2(ARBEIDSTAKER_FNR, HttpStatus.OK)
        val emailContent = emailContentController.getEmailContent(EmailContentControllerV2.BEKREFTET, uuid.toString(), "1")
        Assert.assertNotNull(emailContent.emne)
        Assert.assertNotNull(emailContent.innhold)
    }

    @Test(expected = ForbiddenException::class)
    fun emailContentNoAccess() {
        mockSvarFraTilgangTilBrukerViaAzureV2(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN)
        emailContentController.getEmailContent(EmailContentControllerV2.BEKREFTET, uuid.toString(), null)
    }

    @Test(expected = RuntimeException::class)
    fun emailContentServerError() {
        mockSvarFraTilgangTilBrukerViaAzureV2(ARBEIDSTAKER_FNR, HttpStatus.INTERNAL_SERVER_ERROR)
        emailContentController.getEmailContent(EmailContentControllerV2.BEKREFTET, uuid.toString(), null)
    }
}

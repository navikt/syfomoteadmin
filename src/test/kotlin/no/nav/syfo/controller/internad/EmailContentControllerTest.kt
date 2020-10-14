package no.nav.syfo.controller.internad

import no.nav.syfo.LocalApplication
import no.nav.syfo.domain.AktorId
import no.nav.syfo.api.ressurser.azuread.EmailContentController
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.service.MoteService
import no.nav.syfo.testhelper.MoteGenerator
import no.nav.syfo.testhelper.OidcTestHelper.loggInnVeilederAzure
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
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
class EmailContentControllerTest : AbstractRessursTilgangTest() {

    @MockBean
    private lateinit var moteService: MoteService
    @MockBean
    private lateinit var pdlConsumer: PdlConsumer

    @Inject
    private lateinit var emailContentController: EmailContentController

    private val moteGenerator = MoteGenerator()
    private val uuid = UUID.randomUUID()
    private val mote = moteGenerator.generateMote(uuid)

    @Before
    fun setup() {
        Mockito.`when`(pdlConsumer.fodselsnummer(AktorId(ARBEIDSTAKER_AKTORID))).thenReturn(Fodselsnummer(ARBEIDSTAKER_FNR))
        try {
            loggInnVeilederAzure(oidcRequestContextHolder, VEILEDER_ID)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        Mockito.`when`(moteService.findMoteByMotedeltakerUuid(uuid.toString())).thenReturn(mote)
    }

    @Test
    fun emailContentHasAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.OK)
        val emailContent = emailContentController.getEmailContent(EmailContentController.BEKREFTET, uuid.toString(), "1")
        Assert.assertNotNull(emailContent.emne)
        Assert.assertNotNull(emailContent.innhold)
    }

    @Test(expected = ForbiddenException::class)
    fun emailContentNoAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN)
        emailContentController.getEmailContent(EmailContentController.BEKREFTET, uuid.toString(), null)
    }

    @Test(expected = RuntimeException::class)
    fun emailContentServerError() {
        loggUtAlle(oidcRequestContextHolder)
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.INTERNAL_SERVER_ERROR)
        emailContentController.getEmailContent(EmailContentController.BEKREFTET, uuid.toString(), null)
    }
}

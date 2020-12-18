package no.nav.syfo.controller.internad

import no.nav.syfo.LocalApplication
import no.nav.syfo.history.controller.HistoryController
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.history.HistorikkService
import no.nav.syfo.service.MoteService
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
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class HistoryControllerTest : AbstractRessursTilgangTest() {
    @MockBean
    private lateinit var pdlConsumer: PdlConsumer

    @MockBean
    private lateinit var historikkService: HistorikkService

    @MockBean
    private lateinit var moteService: MoteService

    @Inject
    private lateinit var historyController: HistoryController

    @Before
    @Throws(ParseException::class)
    fun setup() {
        loggInnVeilederAzure(oidcRequestContextHolder, VEILEDER_ID)
    }

    @After
    override fun tearDown() {
        loggUtAlle(oidcRequestContextHolder)
    }

    @Test
    fun historyHasAccess() {
        Mockito.`when`(pdlConsumer.aktorId(Fodselsnummer(ARBEIDSTAKER_FNR))).thenReturn(AktorId(ARBEIDSTAKER_AKTORID))
        Mockito.`when`(moteService.findMoterByBrukerAktoerId(ARBEIDSTAKER_AKTORID)).thenReturn(emptyList())
        Mockito.`when`(historikkService.opprettetHistorikk(emptyList())).thenReturn(emptyList())
        Mockito.`when`(historikkService.flereTidspunktHistorikk(emptyList())).thenReturn(emptyList())
        Mockito.`when`(historikkService.avbruttHistorikk(emptyList())).thenReturn(emptyList())
        Mockito.`when`(historikkService.bekreftetHistorikk(emptyList())).thenReturn(emptyList())
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.OK)
        val history = historyController.getHistory(ARBEIDSTAKER_FNR)
        Assert.assertEquals(emptyList<Any>(), history)
    }

    @Test(expected = ForbiddenException::class)
    fun historyNoAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN)
        historyController.getHistory(ARBEIDSTAKER_FNR)
    }

    @Test(expected = RuntimeException::class)
    fun historyServerError() {
        loggUtAlle(oidcRequestContextHolder)
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.INTERNAL_SERVER_ERROR)
        historyController.getHistory(ARBEIDSTAKER_FNR)
    }
}

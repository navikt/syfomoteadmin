package no.nav.syfo.controller.internad

import no.nav.syfo.LocalApplication
import no.nav.syfo.consumer.aktorregister.AktorregisterConsumer
import no.nav.syfo.consumer.aktorregister.domain.AktorId
import no.nav.syfo.api.ressurser.azuread.AktorController
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
class AktorControllerTilgangTest : AbstractRessursTilgangTest() {
    @Inject
    private lateinit var aktorController: AktorController

    @MockBean
    private lateinit var aktorregisterConsumer: AktorregisterConsumer

    @Before
    fun setup() {
        Mockito.`when`(aktorregisterConsumer.getFnrForAktorId(AktorId(ARBEIDSTAKER_AKTORID))).thenReturn(ARBEIDSTAKER_FNR)
        try {
            loggInnVeilederAzure(oidcRequestContextHolder, VEILEDER_ID)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    @Test
    fun hasAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.OK)
        val aktor = aktorController[ARBEIDSTAKER_AKTORID]
        Assert.assertEquals(ARBEIDSTAKER_FNR, aktor.fnr)
    }

    @Test(expected = ForbiddenException::class)
    fun noAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN)
        aktorController[ARBEIDSTAKER_AKTORID]
    }

    @Test(expected = RuntimeException::class)
    fun invalidUserContext() {
        loggUtAlle(oidcRequestContextHolder)
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN)
        aktorController[ARBEIDSTAKER_AKTORID]
    }
}

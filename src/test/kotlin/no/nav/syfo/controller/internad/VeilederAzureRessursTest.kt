package no.nav.syfo.controller.internad

import no.nav.syfo.LocalApplication
import no.nav.syfo.axsys.AxsysConsumer
import no.nav.syfo.axsys.AxsysEnhet
import no.nav.syfo.controller.internad.veileder.VeilederAzureRessurs
import no.nav.syfo.service.VeilederService
import no.nav.syfo.testhelper.OidcTestHelper.loggInnVeilederAzure
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.NAV_ENHET
import no.nav.syfo.testhelper.UserConstants.NAV_ENHET_NAVN
import no.nav.syfo.testhelper.UserConstants.VEILEDER_ID
import no.nav.syfo.testhelper.UserConstants.VEILEDER_NAVN
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.text.ParseException
import java.util.*
import javax.inject.Inject

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class VeilederAzureRessursTest : AbstractRessursTilgangTest() {
    @MockBean
    private lateinit var axsysConsumer: AxsysConsumer

    @Inject
    private lateinit var veilederAzureRessurs: VeilederAzureRessurs

    @MockBean
    private lateinit var veilederService: VeilederService

    @Before
    fun setup() {
        Mockito.`when`(axsysConsumer.enheter(VEILEDER_ID)).thenReturn(listOf(
            AxsysEnhet(
                NAV_ENHET,
                NAV_ENHET_NAVN
            )
        ))
        try {
            loggInnVeilederAzure(oidcRequestContextHolder, VEILEDER_ID)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        Mockito.`when`(veilederService.hentVeilederNavn(VEILEDER_ID)).thenReturn(Optional.of(VEILEDER_NAVN))
    }

    @Test
    fun hentInnloggetVeilederInfo() {
        val (navn, ident) = veilederAzureRessurs.hentNavn()
        Assert.assertEquals(VEILEDER_NAVN, navn)
        Assert.assertEquals(VEILEDER_ID, ident)
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker_veilederinfo() {
        loggUtAlle(oidcRequestContextHolder)
        veilederAzureRessurs.hentNavn()
    }

    @Test
    fun hentVeilederInfo() {
        val (navn, ident) = veilederAzureRessurs.hentIdent(VEILEDER_ID)
        Assert.assertEquals(VEILEDER_NAVN, navn)
        Assert.assertEquals(VEILEDER_ID, ident)
    }

    @Test
    fun hentInnloggetVeilederEnheter() {
        val (enhetliste) = veilederAzureRessurs.hentEnheter()
        Assert.assertEquals(1, enhetliste.size.toLong())
        Assert.assertEquals(NAV_ENHET_NAVN, enhetliste[0].navn)
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker_veilederenheter() {
        loggUtAlle(oidcRequestContextHolder)
        veilederAzureRessurs.hentEnheter()
    }
}

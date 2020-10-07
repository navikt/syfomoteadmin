package no.nav.syfo.controller.internad

import no.nav.syfo.LocalApplication
import no.nav.syfo.aktorregister.AktorregisterConsumer
import no.nav.syfo.aktorregister.domain.AktorId
import no.nav.syfo.api.ressurser.azuread.PersonController
import no.nav.syfo.dkif.DkifConsumer
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.testhelper.OidcTestHelper.loggInnVeilederAzure
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.PERSON_NAVN
import no.nav.syfo.testhelper.UserConstants.VEILEDER_ID
import no.nav.syfo.testhelper.generateDigitalKontaktinfo
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
class PersonControllerTest : AbstractRessursTilgangTest() {
    @MockBean
    private lateinit var aktorregisterConsumer: AktorregisterConsumer

    @MockBean
    private lateinit var dkifConsumer: DkifConsumer

    @MockBean
    private lateinit var pdlConsumer: PdlConsumer

    @Inject
    private lateinit var personController: PersonController

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
    fun userWithNameHasAccess() {
        Mockito.`when`(pdlConsumer.fullName(ARBEIDSTAKER_FNR)).thenReturn(PERSON_NAVN)
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.OK)
        val user = personController.hentBruker(ARBEIDSTAKER_AKTORID)
        Assert.assertEquals(PERSON_NAVN, user.navn)
    }

    @Test(expected = ForbiddenException::class)
    fun userWithNameNoAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN)
        personController.hentBruker(ARBEIDSTAKER_AKTORID)
    }

    @Test(expected = RuntimeException::class)
    fun userWithNameServerError() {
        loggUtAlle(oidcRequestContextHolder)
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.INTERNAL_SERVER_ERROR)
        personController.hentBruker(ARBEIDSTAKER_AKTORID)
    }

    @Test
    fun userHasAccess() {
        val digitalKontaktinfo = generateDigitalKontaktinfo()
        Mockito.`when`(dkifConsumer.kontaktinformasjon(ARBEIDSTAKER_FNR)).thenReturn(digitalKontaktinfo)
        Mockito.`when`(pdlConsumer.fullName(ARBEIDSTAKER_FNR)).thenReturn(PERSON_NAVN)
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.OK)
        val user = personController.bruker(ARBEIDSTAKER_AKTORID)
        Assert.assertEquals(digitalKontaktinfo.epostadresse, user.kontaktinfo.epost)
        Assert.assertEquals(digitalKontaktinfo.mobiltelefonnummer, user.kontaktinfo.tlf)
        Assert.assertEquals(digitalKontaktinfo.kanVarsles, user.kontaktinfo.reservasjon.skalHaVarsel)
        Assert.assertEquals(PERSON_NAVN, user.navn)
    }

    @Test(expected = ForbiddenException::class)
    fun userNoAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN)
        personController.bruker(ARBEIDSTAKER_AKTORID)
    }

    @Test(expected = RuntimeException::class)
    fun userServerError() {
        loggUtAlle(oidcRequestContextHolder)
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.INTERNAL_SERVER_ERROR)
        personController.bruker(ARBEIDSTAKER_AKTORID)
    }
}

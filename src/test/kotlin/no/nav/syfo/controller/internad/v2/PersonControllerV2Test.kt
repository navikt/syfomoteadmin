package no.nav.syfo.controller.internad.v2

import no.nav.syfo.LocalApplication
import no.nav.syfo.consumer.azuread.v2.AzureAdV2TokenConsumer
import no.nav.syfo.consumer.dkif.DkifConsumer
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.controller.internad.AbstractRessursTilgangTest
import no.nav.syfo.controller.internad.person.v2.PersonControllerV2
import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.testhelper.OidcTestHelper.loggInnVeilederAzureV2
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.PERSON_NAVN
import no.nav.syfo.testhelper.UserConstants.VEILEDER_ID
import no.nav.syfo.testhelper.generateDigitalKontaktinfo
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.HttpServerErrorException
import java.text.ParseException
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class PersonControllerV2Test : AbstractRessursTilgangTest() {
    @MockBean
    private lateinit var azureAdV2TokenConsumer: AzureAdV2TokenConsumer

    @MockBean
    private lateinit var dkifConsumer: DkifConsumer

    @MockBean
    private lateinit var pdlConsumer: PdlConsumer

    @Inject
    private lateinit var personController: PersonControllerV2

    @Before
    fun setup() {
        Mockito.`when`(azureAdV2TokenConsumer.getOnBehalfOfToken(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(oboToken)
        Mockito.`when`(pdlConsumer.fodselsnummer(AktorId(ARBEIDSTAKER_AKTORID))).thenReturn(Fodselsnummer(ARBEIDSTAKER_FNR))
        try {
            loggInnVeilederAzureV2(oidcRequestContextHolder, VEILEDER_ID)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    @Test
    fun userWithNameHasAccess() {
        Mockito.`when`(pdlConsumer.fullName(ARBEIDSTAKER_FNR)).thenReturn(PERSON_NAVN)
        mockSvarFraTilgangTilBrukerViaAzureV2(ARBEIDSTAKER_FNR, HttpStatus.OK)
        val user = personController.hentBruker(ARBEIDSTAKER_AKTORID)
        Assert.assertEquals(PERSON_NAVN, user.navn)
    }

    @Test(expected = ForbiddenException::class)
    fun userWithNameNoAccess() {
        mockSvarFraTilgangTilBrukerViaAzureV2(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN)
        personController.hentBruker(ARBEIDSTAKER_AKTORID)
    }

    @Test(expected = HttpServerErrorException::class)
    fun userWithNameServerError() {
        mockSvarFraTilgangTilBrukerViaAzureV2(ARBEIDSTAKER_FNR, HttpStatus.INTERNAL_SERVER_ERROR)
        personController.hentBruker(ARBEIDSTAKER_AKTORID)
    }

    @Test
    fun userHasAccess() {
        val digitalKontaktinfo = generateDigitalKontaktinfo()
        Mockito.`when`(dkifConsumer.kontaktinformasjon(ARBEIDSTAKER_FNR)).thenReturn(digitalKontaktinfo)
        Mockito.`when`(pdlConsumer.fullName(ARBEIDSTAKER_FNR)).thenReturn(PERSON_NAVN)
        mockSvarFraTilgangTilBrukerViaAzureV2(ARBEIDSTAKER_FNR, HttpStatus.OK)
        val user = personController.bruker(ARBEIDSTAKER_AKTORID)
        Assert.assertEquals(digitalKontaktinfo.epostadresse, user.kontaktinfo?.epost)
        Assert.assertEquals(digitalKontaktinfo.mobiltelefonnummer, user.kontaktinfo?.tlf)
        Assert.assertEquals(digitalKontaktinfo.kanVarsles, user.kontaktinfo?.reservasjon?.skalHaVarsel)
        Assert.assertEquals(PERSON_NAVN, user.navn)
    }

    @Test(expected = ForbiddenException::class)
    fun userNoAccess() {
        mockSvarFraTilgangTilBrukerViaAzureV2(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN)
        personController.bruker(ARBEIDSTAKER_AKTORID)
    }
}

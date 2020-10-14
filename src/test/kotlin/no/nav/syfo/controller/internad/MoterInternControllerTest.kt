package no.nav.syfo.controller.internad

import no.nav.syfo.LocalApplication
import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.api.domain.nyttmoterequest.RSNyttMoteRequest
import no.nav.syfo.api.ressurser.azuread.MoterInternController
import no.nav.syfo.consumer.axsys.AxsysConsumer
import no.nav.syfo.consumer.axsys.AxsysEnhet
import no.nav.syfo.domain.model.*
import no.nav.syfo.metric.Metric
import no.nav.syfo.consumer.narmesteleder.NarmesteLederConsumer
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.repository.dao.MotedeltakerDAO
import no.nav.syfo.repository.dao.TidOgStedDAO
import no.nav.syfo.repository.model.PMotedeltakerAktorId
import no.nav.syfo.repository.model.PMotedeltakerArbeidsgiver
import no.nav.syfo.service.*
import no.nav.syfo.service.varselinnhold.ArbeidsgiverVarselService
import no.nav.syfo.service.varselinnhold.SykmeldtVarselService
import no.nav.syfo.consumer.sts.StsConsumer
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.OidcTestHelper.loggInnVeilederAzure
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.LEDER_AKTORID
import no.nav.syfo.testhelper.UserConstants.LEDER_FNR
import no.nav.syfo.testhelper.UserConstants.NAV_ENHET
import no.nav.syfo.testhelper.UserConstants.NAV_ENHET_NAVN
import no.nav.syfo.testhelper.UserConstants.VEILEDER_ID
import no.nav.syfo.testhelper.UserConstants.VEILEDER_NAVN
import no.nav.syfo.consumer.veiledertilgang.VeilederTilgangConsumer
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cache.CacheManager
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.RestTemplate
import java.text.ParseException
import java.time.LocalDateTime
import java.util.*
import java.util.function.Consumer
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class MoterInternControllerTest : AbstractRessursTilgangTest() {
    @Value("\${syfobehandlendeenhet.url}")
    private lateinit var behandlendeenhetUrl: String

    @Value("\${security.token.service.rest.url}")
    private lateinit var stsUrl: String

    @Value("\${srv.username}")
    private lateinit var srvUsername: String

    @Value("\${srv.password}")
    private lateinit var srvPassword: String

    @MockBean
    private lateinit var moteService: MoteService

    @MockBean
    private lateinit var metric: Metric

    @MockBean
    private lateinit var tidOgStedDAO: TidOgStedDAO

    @MockBean
    private lateinit var hendelseService: HendelseService

    @MockBean
    private lateinit var motedeltakerDAO: MotedeltakerDAO

    @MockBean
    private lateinit var axsysConsumer: AxsysConsumer

    @MockBean
    private lateinit var pdlConsumer: PdlConsumer

    @MockBean
    private lateinit var veilederService: VeilederService

    @MockBean
    private lateinit var arbeidsgiverVarselService: ArbeidsgiverVarselService

    @MockBean
    private lateinit var narmesteLederConsumer: NarmesteLederConsumer

    @MockBean
    private lateinit var sykmeldtVarselService: SykmeldtVarselService

    @MockBean
    private lateinit var tilgangService: VeilederTilgangConsumer

    @Inject
    private lateinit var cacheManager: CacheManager

    @Inject
    private lateinit var stsConsumer: StsConsumer

    @Inject
    private lateinit var restTemplate: RestTemplate

    @Inject
    private lateinit var moterController: MoterInternController

    private lateinit var mockRestServiceServer: MockRestServiceServer

    @Before
    @Throws(ParseException::class)
    fun setup() {
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
        loggInnVeilederAzure(oidcRequestContextHolder, VEILEDER_ID)
        Mockito.`when`(pdlConsumer.aktorId(Fodselsnummer(ARBEIDSTAKER_FNR))).thenReturn(AktorId(ARBEIDSTAKER_AKTORID))
        Mockito.`when`(pdlConsumer.fodselsnummer(AktorId(ARBEIDSTAKER_AKTORID))).thenReturn(Fodselsnummer(ARBEIDSTAKER_FNR))
        Mockito.`when`(pdlConsumer.fodselsnummer(AktorId(AKTOER_ID_2))).thenReturn(Fodselsnummer(FNR_2))
        Mockito.`when`(pdlConsumer.fodselsnummer(AktorId(LEDER_AKTORID))).thenReturn(Fodselsnummer(LEDER_FNR))
        Mockito.`when`(moteService.findMoterByBrukerNavEnhet(NAV_ENHET)).thenReturn(MoteList)
        Mockito.`when`(moteService.maxTwoMonthOldMoterEnhet(NAV_ENHET)).thenReturn(MoteList)
    }

    @After
    fun cleanUp() {
        mockRestServiceServer.reset()
        cacheManager.cacheNames
            .forEach(Consumer { cacheName: String -> cacheManager.getCache(cacheName).clear() })
    }

    private val MoteList = listOf(
        Mote()
            .id(1337L)
            .status(MoteStatus.OPPRETTET)
            .opprettetTidspunkt(LocalDateTime.now().minusMonths(1L))
            .motedeltakere(listOf(MotedeltakerAktorId().aktorId(ARBEIDSTAKER_AKTORID))),
        Mote()
            .id(1338L)
            .status(MoteStatus.OPPRETTET)
            .opprettetTidspunkt(LocalDateTime.now().minusMonths(2L))
            .motedeltakere(listOf(MotedeltakerAktorId().aktorId(AKTOER_ID_2)))
    )

    @Test
    fun hentMoter_fnr_veileder_har_tilgang() {
        val Mote1 = Mote()
            .id(1337L)
            .status(MoteStatus.OPPRETTET)
            .opprettetTidspunkt(LocalDateTime.now().minusMonths(1L))
            .motedeltakere(listOf(MotedeltakerAktorId().aktorId(ARBEIDSTAKER_AKTORID)))
        val Mote2 = Mote()
            .id(1338L)
            .status(MoteStatus.OPPRETTET)
            .opprettetTidspunkt(LocalDateTime.now().minusMonths(2L))
            .motedeltakere(listOf(MotedeltakerAktorId().aktorId(ARBEIDSTAKER_AKTORID)))
        Mockito.`when`(tilgangService.hasVeilederAccessToPerson(ARBEIDSTAKER_FNR)).thenReturn(true)
        Mockito.`when`(moteService.findMoterByBrukerAktoerId(ARBEIDSTAKER_AKTORID)).thenReturn(listOf(Mote1, Mote2))
        Mockito.`when`(hendelseService.sistEndretMoteStatus(ArgumentMatchers.anyLong())).thenReturn(Optional.empty())
        val moteList = moterController.hentMoter(null, ARBEIDSTAKER_FNR, false, null, false)
        Assert.assertEquals(ARBEIDSTAKER_AKTORID, moteList[0].aktorId)
        Assert.assertEquals(ARBEIDSTAKER_AKTORID, moteList[1].aktorId)
        Mockito.verify(pdlConsumer, Mockito.times(4)).fodselsnummer(AktorId(ARBEIDSTAKER_AKTORID))
        Mockito.verify(pdlConsumer, Mockito.times(1)).aktorId(Fodselsnummer(ARBEIDSTAKER_FNR))
        Mockito.verify(moteService).findMoterByBrukerAktoerId(ARBEIDSTAKER_AKTORID)
    }

    @Test(expected = ForbiddenException::class)
    fun hentMoter_fnr_veileder_har_ikke_tilgang_pga_rolle() {
        Mockito.`when`(tilgangService.hasVeilederAccessToPerson(ARBEIDSTAKER_FNR)).thenReturn(false)
        Mockito.`when`(pdlConsumer.isKode6Or7(ARBEIDSTAKER_FNR)).thenReturn(false)
        moterController.hentMoter(null, ARBEIDSTAKER_FNR, false, null, false)
        Mockito.verify(pdlConsumer).isKode6Or7(ARBEIDSTAKER_FNR)
    }

    @Test(expected = ForbiddenException::class)
    fun hentMoter_fnr_veileder_har_ikke_tilgang_pga_skjermet_bruker() {
        Mockito.`when`(tilgangService.hasVeilederAccessToPerson(ARBEIDSTAKER_FNR)).thenReturn(true)
        Mockito.`when`(pdlConsumer.isKode6Or7(ARBEIDSTAKER_FNR)).thenReturn(true)
        moterController.hentMoter(null, ARBEIDSTAKER_FNR, false, null, false)
    }

    @Test(expected = RuntimeException::class)
    fun hentMoter_fnr_veileder_annen_tilgangsfeil() {
        Mockito.`when`(tilgangService.hasVeilederAccessToPerson(ARBEIDSTAKER_FNR)).thenReturn(false)
        Mockito.`when`(pdlConsumer.isKode6Or7(ARBEIDSTAKER_FNR)).thenReturn(false)
        moterController.hentMoter(null, ARBEIDSTAKER_FNR, false, null, false)
        Mockito.verify(pdlConsumer).isKode6Or7(ARBEIDSTAKER_FNR)
    }

    @Test
    fun hentMoter_navenhet_veileder_har_full_tilgang() {
        Mockito.`when`(tilgangService.hasVeilederAccessToPerson(ARBEIDSTAKER_FNR)).thenReturn(true)
        Mockito.`when`(tilgangService.hasVeilederAccessToPerson(FNR_2)).thenReturn(true)
        Mockito.`when`(axsysConsumer.enheter(VEILEDER_ID)).thenReturn(listOf(
            AxsysEnhet(
                NAV_ENHET,
                NAV_ENHET_NAVN
            )
        ))
        Mockito.`when`(pdlConsumer.isKode6Or7(ARBEIDSTAKER_FNR)).thenReturn(false)
        Mockito.`when`(pdlConsumer.isKode6Or7(FNR_2)).thenReturn(false)
        Mockito.`when`(hendelseService.sistEndretMoteStatus(ArgumentMatchers.anyLong())).thenReturn(Optional.empty())
        val moteList = moterController.hentMoter(null, null, false, NAV_ENHET, false)
        Assert.assertEquals(2, moteList.size.toLong())
        Assert.assertEquals(ARBEIDSTAKER_AKTORID, moteList[0].aktorId)
        Assert.assertEquals(AKTOER_ID_2, moteList[1].aktorId)
        Mockito.verify(pdlConsumer).isKode6Or7(ARBEIDSTAKER_FNR)
        Mockito.verify(pdlConsumer).isKode6Or7(FNR_2)
    }

    @Test
    fun hentMoter_navenhet_veileder_har_delvis_tilgang_pga_rolle() {
        Mockito.`when`(tilgangService.hasVeilederAccessToPerson(ArgumentMatchers.anyString()))
            .thenReturn(true)
            .thenReturn(false)
        Mockito.`when`(axsysConsumer.enheter(VEILEDER_ID)).thenReturn(listOf(
            AxsysEnhet(
                NAV_ENHET,
                NAV_ENHET_NAVN
            )
        ))
        Mockito.`when`(pdlConsumer.isKode6Or7(ARBEIDSTAKER_FNR)).thenReturn(false)
        Mockito.`when`(pdlConsumer.isKode6Or7(FNR_2)).thenReturn(false)
        Mockito.`when`(hendelseService.sistEndretMoteStatus(ArgumentMatchers.anyLong())).thenReturn(Optional.empty())
        val moteList = moterController.hentMoter(null, null, false, NAV_ENHET, false)
        Assert.assertEquals(1, moteList.size.toLong())
        Assert.assertEquals(ARBEIDSTAKER_AKTORID, moteList[0].aktorId)
        Mockito.verify(pdlConsumer).isKode6Or7(ARBEIDSTAKER_FNR)
        Mockito.verify(pdlConsumer).isKode6Or7(FNR_2)
    }

    @Test
    fun hentMoter_navenhet_veileder_har_delvis_tilgang_pga_skjermet_bruker() {
        Mockito.`when`(tilgangService.hasVeilederAccessToPerson(ARBEIDSTAKER_FNR)).thenReturn(true)
        Mockito.`when`(axsysConsumer.enheter(VEILEDER_ID)).thenReturn(listOf(
            AxsysEnhet(
                NAV_ENHET,
                NAV_ENHET_NAVN
            )
        ))
        Mockito.`when`(pdlConsumer.isKode6Or7(ARBEIDSTAKER_FNR)).thenReturn(false)
        Mockito.`when`(pdlConsumer.isKode6Or7(FNR_2)).thenReturn(true)
        Mockito.`when`(hendelseService.sistEndretMoteStatus(ArgumentMatchers.anyLong())).thenReturn(Optional.empty())
        val moteList = moterController.hentMoter(null, null, false, NAV_ENHET, false)
        Assert.assertEquals(1, moteList.size.toLong())
        Assert.assertEquals(ARBEIDSTAKER_AKTORID, moteList[0].aktorId)
        Mockito.verify(pdlConsumer).isKode6Or7(ARBEIDSTAKER_FNR)
        Mockito.verify(pdlConsumer).isKode6Or7(FNR_2)
    }

    @Test
    fun hentMoter_navenhet_veileder_har_ikke_tilgang_pga_rolle() {
        Mockito.`when`(tilgangService.hasVeilederAccessToPerson(ARBEIDSTAKER_FNR)).thenReturn(false)
        Mockito.`when`(axsysConsumer.enheter(VEILEDER_ID)).thenReturn(listOf(
            AxsysEnhet(
                NAV_ENHET,
                NAV_ENHET_NAVN
            )
        ))
        Mockito.`when`(pdlConsumer.isKode6Or7(ARBEIDSTAKER_FNR)).thenReturn(false)
        Mockito.`when`(pdlConsumer.isKode6Or7(FNR_2)).thenReturn(false)
        Mockito.`when`(hendelseService.sistEndretMoteStatus(ArgumentMatchers.anyLong())).thenReturn(Optional.empty())
        val moteList = moterController.hentMoter(null, null, false, NAV_ENHET, false)
        Assert.assertEquals(0, moteList.size.toLong())
        Mockito.verify(pdlConsumer).isKode6Or7(ARBEIDSTAKER_FNR)
        Mockito.verify(pdlConsumer).isKode6Or7(FNR_2)
    }

    @Test
    fun hentMoter_navenhet_veileder_har_ikke_tilgang_pga_skjerming() {
        Mockito.`when`(tilgangService.hasVeilederAccessToPerson(ARBEIDSTAKER_FNR)).thenReturn(true)
        Mockito.`when`(axsysConsumer.enheter(VEILEDER_ID)).thenReturn(listOf(
            AxsysEnhet(
                NAV_ENHET,
                NAV_ENHET_NAVN
            )
        ))
        Mockito.`when`(pdlConsumer.isKode6Or7(ARBEIDSTAKER_FNR)).thenReturn(true)
        Mockito.`when`(pdlConsumer.isKode6Or7(FNR_2)).thenReturn(true)
        Mockito.`when`(hendelseService.sistEndretMoteStatus(ArgumentMatchers.anyLong())).thenReturn(Optional.empty())
        val moteList = moterController.hentMoter(null, null, false, NAV_ENHET, false)
        Assert.assertEquals(0, moteList.size.toLong())
        Mockito.verify(pdlConsumer).isKode6Or7(ARBEIDSTAKER_FNR)
        Mockito.verify(pdlConsumer).isKode6Or7(FNR_2)
    }

    @Test(expected = RuntimeException::class)
    fun hentMoter_navenhet_annen_tilgangsfeil() {
        Mockito.`when`(tilgangService.hasVeilederAccessToPerson(ARBEIDSTAKER_FNR)).thenReturn(true)
        Mockito.doThrow(RuntimeException()).`when`(tilgangService).hasVeilederAccessToPerson(ARBEIDSTAKER_FNR)
        Mockito.`when`(axsysConsumer.enheter(VEILEDER_ID)).thenReturn(listOf(
            AxsysEnhet(
                NAV_ENHET,
                NAV_ENHET_NAVN
            )
        ))
        Mockito.`when`(pdlConsumer.isKode6Or7(ARBEIDSTAKER_FNR)).thenReturn(false)
        Mockito.`when`(pdlConsumer.isKode6Or7(FNR_2)).thenReturn(false)
        Mockito.`when`(hendelseService.sistEndretMoteStatus(ArgumentMatchers.anyLong())).thenReturn(Optional.empty())
        moterController.hentMoter(null, null, false, NAV_ENHET, false)
        Mockito.verify(pdlConsumer).isKode6Or7(ARBEIDSTAKER_FNR)
        Mockito.verify(pdlConsumer).isKode6Or7(FNR_2)
    }

    @Test
    fun opprettMoter_har_tilgang() {
        val nyttMoteRequest = RSNyttMoteRequest()
            .fnr(ARBEIDSTAKER_FNR)
            .orgnummer("123")
        mockBehandlendEnhet(ARBEIDSTAKER_FNR)
        Mockito.`when`(tilgangService.hasVeilederAccessToPerson(ARBEIDSTAKER_FNR)).thenReturn(true)
        Mockito.`when`(pdlConsumer.isKode6Or7(ARBEIDSTAKER_FNR)).thenReturn(false)
        Mockito.`when`(pdlConsumer.fullName(LEDER_AKTORID)).thenReturn("Frida Frisk")
        Mockito.`when`(narmesteLederConsumer.narmesteLederRelasjonLeder(ARBEIDSTAKER_AKTORID, nyttMoteRequest.orgnummer)).thenReturn(
            generateNarmesteLederRelasjon()
        )
        Mockito.`when`(moteService.opprettMote(ArgumentMatchers.any())).thenReturn(Mote().id(1L))
        Mockito.`when`(tidOgStedDAO.create(ArgumentMatchers.any())).thenReturn(TidOgSted())
        Mockito.`when`(motedeltakerDAO.create(ArgumentMatchers.any(PMotedeltakerAktorId::class.java))).thenReturn(MotedeltakerAktorId())
        Mockito.`when`(motedeltakerDAO.create(ArgumentMatchers.any(PMotedeltakerArbeidsgiver::class.java))).thenReturn(MotedeltakerArbeidsgiver())
        Mockito.`when`(veilederService.hentVeilederNavn(ArgumentMatchers.any())).thenReturn(Optional.of(VEILEDER_NAVN))
        moterController.opprett(nyttMoteRequest)
        Mockito.verify(pdlConsumer).isKode6Or7(ARBEIDSTAKER_FNR)
    }

    @Test(expected = ForbiddenException::class)
    fun opprettMoter_ikke_tilgang_pga_skjermet_bruker() {
        Mockito.`when`(tilgangService.hasVeilederAccessToPerson(ARBEIDSTAKER_FNR)).thenReturn(true)
        Mockito.`when`(pdlConsumer.isKode6Or7(ARBEIDSTAKER_FNR)).thenReturn(true)
        moterController.opprett(RSNyttMoteRequest().fnr(ARBEIDSTAKER_FNR))
    }

    @Test(expected = ForbiddenException::class)
    fun opprettMoter_ikke_tilgang_pga_rolle() {
        Mockito.`when`(tilgangService.hasVeilederAccessToPerson(ARBEIDSTAKER_FNR)).thenReturn(false)
        Mockito.`when`(pdlConsumer.isKode6Or7(ARBEIDSTAKER_FNR)).thenReturn(false)
        moterController.opprett(RSNyttMoteRequest().fnr(ARBEIDSTAKER_FNR))
    }

    @Test(expected = RuntimeException::class)
    fun opprettMoter_annen_tilgangsfeil() {
        Mockito.doThrow(RuntimeException()).`when`(tilgangService).hasVeilederAccessToPerson(ARBEIDSTAKER_FNR)
        Mockito.`when`(pdlConsumer.isKode6Or7(ARBEIDSTAKER_FNR)).thenReturn(false)
        moterController.opprett(RSNyttMoteRequest().fnr(ARBEIDSTAKER_FNR))
    }

    private fun mockSTS() {
        if (!stsConsumer.isTokenCached()) {
            mockAndExpectSTSService(mockRestServiceServer, stsUrl, srvUsername, srvPassword)
        }
    }

    private fun mockBehandlendEnhet(fnr: String) {
        mockSTS()
        mockAndExpectBehandlendeEnhetRequest(mockRestServiceServer, behandlendeenhetUrl, fnr)
    }

    companion object {
        private const val AKTOER_ID_2 = "1101010101010"
        private const val FNR_2 = "11010101010"
    }
}

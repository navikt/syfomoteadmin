package no.nav.syfo.controller.internad

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.LocalApplication
import no.nav.syfo.api.domain.nyttmoterequest.RSNyttAlternativ
import no.nav.syfo.api.mappers.RSNyttMoteMapper
import no.nav.syfo.domain.model.TidOgSted
import no.nav.syfo.service.MoteService
import no.nav.syfo.testhelper.OidcTestHelper.loggInnVeilederAzure
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.VEILEDER_ID
import no.nav.syfo.util.MapUtil
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.text.ParseException
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class MoteActionsControllerTest {

    @MockBean
    private lateinit var moteService: MoteService

    @Inject
    private lateinit var oidcRequestContextHolder: OIDCRequestContextHolder

    @Inject
    private lateinit var moteActionsController: MoteActionsController

    @Before
    @Throws(ParseException::class)
    fun setup() {
        loggInnVeilederAzure(oidcRequestContextHolder, VEILEDER_ID)
    }

    @Test
    fun avbrytDialogmoteHasAccess() {
        val uuid = UUID.randomUUID()
        val varsle = true

        moteActionsController.avbryt(uuid.toString(), varsle)

        verify(moteService).avbrytMote(uuid.toString(), varsle, VEILEDER_ID)
    }

    @Test(expected = RuntimeException::class)
    fun avbrytDialogmoteServerError() {
        loggUtAlle(oidcRequestContextHolder)

        moteActionsController.avbryt(UUID.randomUUID().toString(), true)
    }

    @Test
    fun bekreftDialogmoteHasAccess() {
        val uuid = UUID.randomUUID()
        val alternativId = 1L

        moteActionsController.bekreft(uuid.toString(), alternativId)

        verify(moteService).bekreftMote(uuid.toString(), alternativId, VEILEDER_ID)
    }

    @Test(expected = RuntimeException::class)
    fun bekreftDialogmoteServerError() {
        loggUtAlle(oidcRequestContextHolder)

        moteActionsController.bekreft(UUID.randomUUID().toString(), 1L)
    }

    @Test
    fun nyeAlternativerDialogmoteHasAccess() {
        val uuid = UUID.randomUUID()
        val alternativ = RSNyttAlternativ()
        alternativ.tid = LocalDateTime.now().toString()
        alternativ.sted = "Testveien 2"
        val alternativeListe = listOf(
                alternativ
        )
        val tidStedListe = MapUtil.mapListe(
                alternativeListe,
                RSNyttMoteMapper.opprett2TidOgSted
        )

        moteActionsController.nyeAlternativer(uuid.toString(), alternativeListe)

        verify(moteService).nyeAlternativer(uuid.toString(), tidStedListe, VEILEDER_ID)
    }

    @Test(expected = RuntimeException::class)
    fun nyeAlternativerDialogmoteServerError() {
        loggUtAlle(oidcRequestContextHolder)

        moteActionsController.nyeAlternativer(UUID.randomUUID().toString(), emptyList())
    }
}


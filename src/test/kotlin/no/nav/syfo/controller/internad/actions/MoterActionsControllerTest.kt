package no.nav.syfo.controller.internad.actions

import no.nav.syfo.LocalApplication
import no.nav.syfo.api.domain.RSOverforMoter
import no.nav.syfo.api.ressurser.azuread.actions.MoterActionsController
import no.nav.syfo.controller.internad.AbstractRessursTilgangTest
import no.nav.syfo.service.MoteService
import no.nav.syfo.testhelper.OidcTestHelper.loggInnVeilederAzure
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.VEILEDER_ID
import org.junit.Before
import org.junit.Test
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
class MoterActionsControllerTest : AbstractRessursTilgangTest() {
    @MockBean
    private lateinit var moteService: MoteService

    @Inject
    lateinit var moterActionsController: MoterActionsController

    @Before
    @Throws(ParseException::class)
    fun setup() {
        loggInnVeilederAzure(oidcRequestContextHolder, VEILEDER_ID)
    }

    @Test
    fun transferDialogmoterHasAccess() {
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()
        val rsOverforMoter = getRSOverforMote(listOf(
            uuid1.toString(),
            uuid2.toString()
        ))
        moterActionsController.transferDialogmoter(rsOverforMoter)
        Mockito.verify(moteService, Mockito.times(1)).overforMoteTil(uuid1.toString(), VEILEDER_ID)
        Mockito.verify(moteService, Mockito.times(1)).overforMoteTil(uuid2.toString(), VEILEDER_ID)
    }

    @Test(expected = RuntimeException::class)
    fun transferDialogmoterServerError() {
        loggUtAlle(oidcRequestContextHolder)
        val rsOverforMoter = getRSOverforMote(listOf(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        ))
        moterActionsController.transferDialogmoter(rsOverforMoter)
    }

    private fun getRSOverforMote(uuidList: List<String>): RSOverforMoter {
        val rsOverforMoter = RSOverforMoter()
        rsOverforMoter.moteUuidListe = uuidList
        return rsOverforMoter
    }
}

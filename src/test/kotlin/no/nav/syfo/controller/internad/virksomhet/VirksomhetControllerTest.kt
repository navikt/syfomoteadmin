package no.nav.syfo.controller.internad.virksomhet

import no.nav.syfo.LocalApplication
import no.nav.syfo.ereg.EregConsumer
import no.nav.syfo.testhelper.UserConstants
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import javax.inject.Inject

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class VirksomhetControllerTest {
    @MockBean
    private lateinit var eregConsumer: EregConsumer

    @Inject
    private lateinit var virksomhetController: VirksomhetController

    @Before
    fun setup() {
        `when`(eregConsumer.virksomhetsnavn(UserConstants.VIRKSOMHETSNUMMER)).thenReturn(UserConstants.VIRKSOMHET_NAME)
    }

    @Test
    fun userWithNameHasAccess() {
        val virksomhet = virksomhetController.getVirksomhetsnavn(UserConstants.VIRKSOMHETSNUMMER)
        Assert.assertEquals(UserConstants.VIRKSOMHET_NAME, virksomhet.navn)
    }
}

package no.nav.syfo.service

import no.nav.syfo.brukertilgang.BrukertilgangConsumer
import no.nav.syfo.pdl.PdlConsumer
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BrukertilgangskontrollServiceTest {
    @Mock
    private lateinit var brukertilgangConsumer: BrukertilgangConsumer

    @Mock
    private lateinit var pdlConsumer: PdlConsumer

    @InjectMocks
    private lateinit var brukertilgangService: BrukertilgangService

    @Test
    fun harTilgangTilOppslaattBrukerGirFalseNaarOppslaattBrukerErKode6() {
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(SPOR_OM_FNR)).thenReturn(true)
        Mockito.`when`(pdlConsumer.isKode6(SPOR_OM_FNR)).thenReturn(true)
        val tilgang = brukertilgangService.harTilgangTilOppslaattBruker(INNLOGGET_FNR, SPOR_OM_FNR)
        Assertions.assertThat(tilgang).isFalse
    }

    @Test
    fun harTilgangTilOppslaattBrukerGirTrueNaarManSporOmSegSelv() {
        val tilgang = brukertilgangService.harTilgangTilOppslaattBruker(SPOR_OM_FNR, SPOR_OM_FNR)
        Assertions.assertThat(tilgang).isTrue
    }

    @Test
    fun harTilgangTilOppslaattBrukerGirTrueNaarManSporOmEnAnsatt() {
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(SPOR_OM_FNR)).thenReturn(true)
        val tilgang = brukertilgangService.harTilgangTilOppslaattBruker(INNLOGGET_FNR, SPOR_OM_FNR)
        Assertions.assertThat(tilgang).isTrue
    }

    @Test
    fun harTilgangTilOppslaattBrukerGirFalseNaarManSporOmEnSomIkkeErSegSelvOgIkkeAnsatt() {
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(SPOR_OM_FNR)).thenReturn(false)
        val tilgang = brukertilgangService.harTilgangTilOppslaattBruker(INNLOGGET_FNR, SPOR_OM_FNR)
        Assertions.assertThat(tilgang).isFalse
    }

    @Test
    fun sporOmNoenAndreEnnSegSelvGirFalseNaarManSporOmSegSelv() {
        val tilgang = brukertilgangService.sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(INNLOGGET_FNR, INNLOGGET_FNR)
        Assertions.assertThat(tilgang).isFalse
    }

    @Test
    fun sporOmNoenAndreEnnSegSelvGirFalseNaarManSporOmEnAnsatt() {
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(SPOR_OM_FNR)).thenReturn(true)
        val tilgang = brukertilgangService.sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(INNLOGGET_FNR, SPOR_OM_FNR)
        Assertions.assertThat(tilgang).isFalse
    }

    @Test
    fun sporOmNoenAndreEnnSegSelvGirTrueNaarManSporOmEnSomIkkeErSegSelvOgIkkeAnsatt() {
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(SPOR_OM_FNR)).thenReturn(false)
        val tilgang = brukertilgangService.sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(INNLOGGET_FNR, SPOR_OM_FNR)
        Assertions.assertThat(tilgang).isTrue
    }

    companion object {
        private const val INNLOGGET_FNR = "12345678901"
        private const val SPOR_OM_FNR = "12345678902"
    }
}

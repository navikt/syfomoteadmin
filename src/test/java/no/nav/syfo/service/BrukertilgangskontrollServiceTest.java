package no.nav.syfo.service;

import no.nav.syfo.brukertilgang.BrukertilgangConsumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BrukertilgangskontrollServiceTest {

    @Mock
    private BrukerprofilService brukerprofilService;
    @Mock
    private BrukertilgangConsumer brukertilgangConsumer;
    @Mock
    private PersonService personService;
    @InjectMocks
    private BrukertilgangService brukertilgangService;

    private static final String INNLOGGET_FNR = "12345678901";
    private static final String SPOR_OM_FNR = "12345678902";

    @Test
    public void harTilgangTilOppslaattBrukerGirFalseNaarOppslaattBrukerErKode6() {
        when(brukertilgangConsumer.hasAccessToAnsatt(SPOR_OM_FNR)).thenReturn(true);
        when(personService.erPersonKode6(SPOR_OM_FNR)).thenReturn(true);

        boolean tilgang = brukertilgangService.harTilgangTilOppslaattBruker(INNLOGGET_FNR, SPOR_OM_FNR);
        assertThat(tilgang).isFalse();
    }

    @Test
    public void harTilgangTilOppslaattBrukerGirTrueNaarManSporOmSegSelv() {
        boolean tilgang = brukertilgangService.harTilgangTilOppslaattBruker(SPOR_OM_FNR, SPOR_OM_FNR);
        assertThat(tilgang).isTrue();
    }

    @Test
    public void harTilgangTilOppslaattBrukerGirTrueNaarManSporOmEnAnsatt() {
        when(brukertilgangConsumer.hasAccessToAnsatt(SPOR_OM_FNR)).thenReturn(true);
        boolean tilgang = brukertilgangService.harTilgangTilOppslaattBruker(INNLOGGET_FNR, SPOR_OM_FNR);
        assertThat(tilgang).isTrue();
    }

    @Test
    public void harTilgangTilOppslaattBrukerGirFalseNaarManSporOmEnSomIkkeErSegSelvOgIkkeAnsatt() {
        when(brukertilgangConsumer.hasAccessToAnsatt(SPOR_OM_FNR)).thenReturn(false);
        boolean tilgang = brukertilgangService.harTilgangTilOppslaattBruker(INNLOGGET_FNR, SPOR_OM_FNR);
        assertThat(tilgang).isFalse();
    }

    @Test
    public void sporOmNoenAndreEnnSegSelvGirFalseNaarManSporOmSegSelv() {
        boolean tilgang = brukertilgangService.sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(INNLOGGET_FNR, INNLOGGET_FNR);
        assertThat(tilgang).isFalse();
    }

    @Test
    public void sporOmNoenAndreEnnSegSelvGirFalseNaarManSporOmEnAnsatt() {
        when(brukertilgangConsumer.hasAccessToAnsatt(SPOR_OM_FNR)).thenReturn(true);
        boolean tilgang = brukertilgangService.sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(INNLOGGET_FNR, SPOR_OM_FNR);
        assertThat(tilgang).isFalse();
    }

    @Test
    public void sporOmNoenAndreEnnSegSelvGirTrueNaarManSporOmEnSomIkkeErSegSelvOgIkkeAnsatt() {
        when(brukertilgangConsumer.hasAccessToAnsatt(SPOR_OM_FNR)).thenReturn(false);
        boolean tilgang = brukertilgangService.sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(INNLOGGET_FNR, SPOR_OM_FNR);
        assertThat(tilgang).isTrue();
    }
}

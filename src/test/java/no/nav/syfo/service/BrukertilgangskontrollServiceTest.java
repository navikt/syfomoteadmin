package no.nav.syfo.service;

import no.nav.syfo.domain.model.Ansatt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class BrukertilgangskontrollServiceTest {

    @Mock
    private AktoerService aktoerService;
    @Mock
    private BrukerprofilService brukerprofilService;
    @Mock
    private SykefravaersoppfoelgingService sykefravaersoppfoelgingService;
    @Mock
    private PersonService personService;
    @InjectMocks
    private BrukertilgangService brukertilgangService;

    private static final String INNLOGGET_FNR = "12345678901";
    private static final String INNLOGGET_AKTOERID = "1234567890123";
    private static final String SPOR_OM_FNR = "12345678902";
    private static final String SPOR_OM_AKTOERID = "1234567890122";

    @Before
    public void setup() {
        when(aktoerService.hentAktoerIdForIdent(INNLOGGET_FNR)).thenReturn(INNLOGGET_AKTOERID);
        when(aktoerService.hentAktoerIdForIdent(SPOR_OM_FNR)).thenReturn(SPOR_OM_AKTOERID);
    }

    @Test
    public void harTilgangTilOppslaattBrukerGirFalseNaarOppslaattBrukerErKode6() {
        when(sykefravaersoppfoelgingService.hentNaermesteLedersAnsattListe(INNLOGGET_AKTOERID)).thenReturn(Collections.singletonList(
                new Ansatt().aktoerId(SPOR_OM_AKTOERID)
        ));
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
        when(sykefravaersoppfoelgingService.hentNaermesteLedersAnsattListe(INNLOGGET_AKTOERID)).thenReturn(Collections.singletonList(
                new Ansatt().aktoerId(SPOR_OM_AKTOERID)
        ));
        boolean tilgang = brukertilgangService.harTilgangTilOppslaattBruker(INNLOGGET_FNR, SPOR_OM_FNR);
        assertThat(tilgang).isTrue();
    }

    @Test
    public void harTilgangTilOppslaattBrukerGirFalseNaarManSporOmEnSomIkkeErSegSelvOgIkkeAnsatt() {
        when(sykefravaersoppfoelgingService.hentNaermesteLedersAnsattListe(INNLOGGET_AKTOERID)).thenReturn(Collections.emptyList());
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
        when(sykefravaersoppfoelgingService.hentNaermesteLedersAnsattListe(INNLOGGET_AKTOERID)).thenReturn(Collections.singletonList(
                new Ansatt().aktoerId(SPOR_OM_AKTOERID)
        ));
        boolean tilgang = brukertilgangService.sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(INNLOGGET_FNR, SPOR_OM_FNR);
        assertThat(tilgang).isFalse();
    }

    @Test
    public void sporOmNoenAndreEnnSegSelvGirTrueNaarManSporOmEnSomIkkeErSegSelvOgIkkeAnsatt() {
        when(sykefravaersoppfoelgingService.hentNaermesteLedersAnsattListe(INNLOGGET_AKTOERID)).thenReturn(Collections.emptyList());
        boolean tilgang = brukertilgangService.sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(INNLOGGET_FNR, SPOR_OM_FNR);
        assertThat(tilgang).isTrue();
    }
}

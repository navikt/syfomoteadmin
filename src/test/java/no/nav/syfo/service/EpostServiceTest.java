package no.nav.syfo.service;

import no.nav.syfo.repository.dao.EpostDAO;
import no.nav.syfo.repository.model.PEpost;
import no.nav.syfo.repository.model.PEpostVedlegg;
import no.nav.syfo.util.Toggle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
//@SpringBootTest(classes = LocalApplication.class)
public class EpostServiceTest {

    @Mock
    private EpostDAO epostDAO;
    @Mock
    private Toggle toggle;
    @InjectMocks
    private EpostService epostService;

    @Before
    public void setUp() {
        when(toggle.toggleSendeEpost()).thenReturn(true);
    }

    @Test
    public void lagrerVedleggene() {
        ArgumentCaptor<PEpost> request = forClass(PEpost.class);
        PEpost epost = new PEpost()
                .mottaker("til@nav.no")
                .emne("emne")
                .innhold("innhold")
                .vedlegg(asList(
                        new PEpostVedlegg()
                                .innhold("innhold")
                                .type("ICS")
                ));

        epostService.klargjorForSending(epost);
        verify(epostDAO, times(1)).create(request.capture());
        assertThat(request.getValue().vedlegg.get(0).innhold).isEqualTo("innhold");
        assertThat(request.getValue().vedlegg.get(0).type).isEqualTo("ICS");
    }
}

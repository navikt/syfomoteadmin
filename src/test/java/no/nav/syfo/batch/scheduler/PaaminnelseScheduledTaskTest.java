package no.nav.syfo.batch.scheduler;

import no.nav.syfo.service.MotedeltakerService;
import no.nav.syfo.util.DatoService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.lang.System.setProperty;
import static java.time.LocalDate.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaaminnelseScheduledTaskTest {

    @Mock
    private MotedeltakerService motedeltakerService;
    @Mock
    private DatoService datoService;
    @InjectMocks
    private PaaminnelseScheduledTask paaminnelseScheduledTask;

    @Before
    public void setup() {
        setProperty("TOGGLE_ENABLE_BATCH_PAAMINNELSE", "true");
    }

    @After
    public void cleanUp() {
        setProperty("TOGGLE_ENABLE_BATCH_PAAMINNELSE", "");
    }

    @Test
    public void senderMedNullDagerEkstraDersomVanligDag() {
        when(datoService.dagensDato()).thenReturn(of(2016, 11, 17));
        paaminnelseScheduledTask.run();
        ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(motedeltakerService, times(1)).findMotedeltakereSomIkkeHarSvartSisteDognet(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()).isEqualTo(0);
    }

    @Test
    public void senderMedToDagerEkstraDersomMandag() {
        when(datoService.dagensDato()).thenReturn(of(2016, 11, 21));
        paaminnelseScheduledTask.run();
        ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(motedeltakerService, times(1)).findMotedeltakereSomIkkeHarSvartSisteDognet(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()).isEqualTo(2);
    }

    @Test
    public void senderMedEnDagerEkstraDersom17Mai() {
        when(datoService.dagensDato()).thenReturn(of(2017, 5, 18));
        paaminnelseScheduledTask.run();
        ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(motedeltakerService, times(1)).findMotedeltakereSomIkkeHarSvartSisteDognet(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()).isEqualTo(1);
    }

    @Test
    public void dagenEtterPaaske2017() {
        when(datoService.dagensDato()).thenReturn(of(2017, 4, 18));
        paaminnelseScheduledTask.run();
        ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(motedeltakerService, times(1)).findMotedeltakereSomIkkeHarSvartSisteDognet(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()).isEqualTo(5);
    }

}

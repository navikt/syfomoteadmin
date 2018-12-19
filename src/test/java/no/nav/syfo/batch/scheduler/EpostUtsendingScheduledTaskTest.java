package no.nav.syfo.batch.scheduler;

import no.nav.syfo.repository.model.PEpost;
import no.nav.syfo.service.EpostService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static java.lang.System.setProperty;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EpostUtsendingScheduledTaskTest {

    @Mock
    private EpostService epostService;
    @InjectMocks
    private EpostUtsendingScheduledTask epostUtsendingScheduledTask;

    @Before
    public void setup() {
        setProperty("LOCAL_MOCK", "false");
    }

    @Test
    public void senderOgLagrerEnDersomBareEn() {
        when(epostService.finnEpostForSending()).thenReturn(Collections.singletonList(
                new PEpost()
                        .id(1L)
                        .innhold("innhold")
                        .emne("emne")
                        .mottaker("test@nav.no")
        ));
        epostUtsendingScheduledTask.run();

        verify(epostService, times(1)).send(any());
        verify(epostService, times(1)).slettEpostEtterSending(anyLong());
    }

    @Test
    public void senderOgLagrerFlere() {
        when(epostService.finnEpostForSending()).thenReturn(asList(
                new PEpost()
                        .id(1L)
                        .innhold("innhold")
                        .emne("emne")
                        .mottaker("test@nav.no"),
                new PEpost()
                        .id(2L)
                        .innhold("innhold")
                        .emne("emne")
                        .mottaker("test@nav.no")
        ));
        epostUtsendingScheduledTask.run();

        verify(epostService, times(2)).send(any());
        verify(epostService, times(2)).slettEpostEtterSending(anyLong());
    }
}

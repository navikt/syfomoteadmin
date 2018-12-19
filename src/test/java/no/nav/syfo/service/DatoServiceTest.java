package no.nav.syfo.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;

import static no.nav.syfo.util.DatoService.dagerMellom;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DatoServiceTest {

    @Test
    public void ekskludererHelgedager() {
        int dagerMellom = dagerMellom(LocalDate.of(2017, 2, 17), LocalDate.of(2017, 2, 20));
        assertThat(dagerMellom).isEqualTo(1);
    }
}

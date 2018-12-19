package no.nav.syfo.service;

import no.nav.syfo.domain.model.MotedeltakerAktorId;
import no.nav.syfo.util.MoterUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MoterUtilTest {

    @Test
    public void sisteSvarErMottatt() {
        boolean erSisteSvarMottatt = MoterUtil.erSisteSvarMottatt(Arrays.asList(
                new MotedeltakerAktorId().svartTidspunkt(now())
        ));

        assertThat(erSisteSvarMottatt).isTrue();
    }

    @Test
    public void sisteSvarErIkkeMottatt() {
        boolean erSisteSvarMottatt = MoterUtil.erSisteSvarMottatt(Arrays.asList(
                new MotedeltakerAktorId().svartTidspunkt(null)
        ));

        assertThat(erSisteSvarMottatt).isFalse();
    }
}

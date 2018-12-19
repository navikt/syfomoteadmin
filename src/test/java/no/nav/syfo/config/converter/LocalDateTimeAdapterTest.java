package no.nav.syfo.config.converter;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalDateTimeAdapterTest {

    LocalDateTimeAdapter adapter = new LocalDateTimeAdapter();

    @Test
    public void unmarshalSommertidMinus1Time() throws Exception {
        String tid = "2016-10-20T21:59:59Z";
        LocalDateTime dateTime = adapter.unmarshal(tid);
        assertThat(dateTime).isEqualTo(LocalDateTime.of(2016, 10, 20, 23, 59, 59));
    }

    @Test
    public void unmarshalSommertid() throws Exception {
        String tid = "2016-10-20T22:00:00Z";
        LocalDateTime dateTime = adapter.unmarshal(tid);
        assertThat(dateTime).isEqualTo(LocalDateTime.of(2016, 10, 21, 0, 0));
    }

    @Test
    public void unmarshalSommertidPluss1Time() throws Exception {
        String tid = "2016-10-20T23:00:00Z";
        LocalDateTime dateTime = adapter.unmarshal(tid);
        assertThat(dateTime).isEqualTo(LocalDateTime.of(2016, 10, 21, 1, 0));
    }

    @Test
    public void unmarshalVintertidMinus1Time() throws Exception {
        String tid = "2016-01-20T22:59:59Z";
        LocalDateTime dateTime = adapter.unmarshal(tid);
        assertThat(dateTime).isEqualTo(LocalDateTime.of(2016, 1, 20, 23, 59, 59));
    }

    @Test
    public void unmarshalVintertid() throws Exception {
        String tid = "2016-01-20T23:00:00Z";
        LocalDateTime dateTime = adapter.unmarshal(tid);
        assertThat(dateTime).isEqualTo(LocalDateTime.of(2016, 1, 21, 0, 0));
    }

    @Test
    public void unmarshalVintertidPluss1Time() throws Exception {
        String tid = "2016-01-21T00:00:00Z";
        LocalDateTime dateTime = adapter.unmarshal(tid);
        assertThat(dateTime).isEqualTo(LocalDateTime.of(2016, 1, 21, 1, 0));
    }

    @Test
    public void marshalSommertidMinus1Time() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2016, 10, 20, 23, 59, 59);
        String tid = adapter.marshal(dateTime);
        assertThat(tid).isEqualTo("2016-10-20T21:59:59Z");
    }

    @Test
    public void marshalSommertid() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2016, 10, 21, 0, 0);
        String tid = adapter.marshal(dateTime);
        assertThat(tid).isEqualTo("2016-10-20T22:00:00Z");
    }

    @Test
    public void marshalSommertidPluss1Time() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2016, 10, 21, 1, 0);
        String tid = adapter.marshal(dateTime);
        assertThat(tid).isEqualTo("2016-10-20T23:00:00Z");
    }

    @Test
    public void marshalVintertidMinus1Time() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2016, 1, 20, 23, 59, 59);
        String tid = adapter.marshal(dateTime);
        assertThat(tid).isEqualTo("2016-01-20T22:59:59Z");
    }

    @Test
    public void marshalVintertid() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2016, 1, 21, 0, 0);
        String tid = adapter.marshal(dateTime);
        assertThat(tid).isEqualTo("2016-01-20T23:00:00Z");
    }

    @Test
    public void marshalVintertidPluss1Time() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2016, 1, 21, 1, 0);
        String tid = adapter.marshal(dateTime);
        assertThat(tid).isEqualTo("2016-01-21T00:00:00Z");
    }

}

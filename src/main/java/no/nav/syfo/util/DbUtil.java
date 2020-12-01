package no.nav.syfo.util;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.*;

import static java.util.Optional.ofNullable;

public class DbUtil {
    public static long nesteSekvensverdi(String sekvensnavn, JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForObject("select " + sekvensnavn + ".nextval from dual", (rs, rowNum) -> rs.getLong(1));
    }

    public static Date convert(LocalDate date) {
        return ofNullable(date).map(Date::valueOf).orElse(null);
    }

    public static LocalDateTime convert(Date date) {
        return ofNullable(date).map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()).orElse(null);
    }

    public static Timestamp convert(LocalDateTime timestamp) {
        return ofNullable(timestamp).map(Timestamp::valueOf).orElse(null);
    }

    public static LocalDateTime convert(Timestamp timestamp) {
        return ofNullable(timestamp).map(Timestamp::toLocalDateTime).orElse(null);
    }
}

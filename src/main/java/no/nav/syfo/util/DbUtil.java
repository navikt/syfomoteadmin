package no.nav.syfo.util;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static java.util.Optional.*;

public class DbUtil {

    public static <T> Optional<T> queryOptional(JdbcTemplate jdbcTemplate, String sql, RowMapper<T> rowMapper, Object... args) {
        try {
            return of(jdbcTemplate.queryForObject(sql, rowMapper, args));
        } catch (DataAccessException e) {
            return empty();
        }
    }

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

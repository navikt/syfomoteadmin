package no.nav.syfo.repository.dao;


import no.nav.syfo.domain.model.TidOgSted;
import no.nav.syfo.repository.model.PTidOgSted;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;
import static no.nav.syfo.repository.mapper.PTidStedMapper.p2TidOgSted;
import static no.nav.syfo.util.DbUtil.convert;
import static no.nav.syfo.util.DbUtil.nesteSekvensverdi;
import static no.nav.syfo.util.MapUtil.map;
import static no.nav.syfo.util.MapUtil.mapListe;

@Transactional
public class TidOgStedDAO {
    @Inject
    private JdbcTemplate jdbcTemplate;
    @Inject
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public TidOgSted finnAlternativ(long tidOgStedId) {
        return map(jdbcTemplate.queryForObject("select * from tid_sted where tid_sted_id = ?", new TidOgStedMapper(), tidOgStedId), p2TidOgSted);
    }

    public List<TidOgSted> finnAlternativer(long moteId) {
        return mapListe(jdbcTemplate.query("select * from tid_sted where mote_id = ?", new TidOgStedMapper(), moteId), p2TidOgSted);
    }

    public List<TidOgSted> finnAlternativerMedBrukersValg(long moteId, long motedeltakerId) {
        return jdbcTemplate.query("select * from tid_sted where mote_id = ?", new TidOgStedMapper(), moteId)
                .stream()
                .map(p2TidOgSted)
                .map(alternativ -> alternativ.valgt(jdbcTemplate.queryForObject("select count(*) from motedeltaker_tid_sted where tid_sted_id = ? and motedeltaker_id = ?",
                        (rs, rowNum) -> rs.getLong(1), alternativ.id, motedeltakerId) > 0))
                .collect(toList());
    }

    public TidOgSted create(TidOgSted tidOgSted) {
        Long nesteSekvensverdi = nesteSekvensverdi("TID_STED_ID_SEQ", jdbcTemplate);
        String sql = "insert into tid_sted " +
                "(tid_sted_id, mote_id, tid, sted, created)" +
                "VALUES (:tid_sted_id, :mote_id, :tid, :sted, :created)";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("tid_sted_id", nesteSekvensverdi)
                .addValue("mote_id", tidOgSted.moteId)
                .addValue("tid", convert(tidOgSted.tid))
                .addValue("sted", tidOgSted.sted)
                .addValue("created", convert(now()));
        namedParameterJdbcTemplate.update(sql, namedParameters);
        return tidOgSted.id(nesteSekvensverdi);
    }

    public static class TidOgStedMapper implements RowMapper<PTidOgSted> {
        public PTidOgSted mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PTidOgSted()
                    .id(rs.getLong("tid_sted_id"))
                    .moteId(rs.getLong("mote_id"))
                    .sted(rs.getString("sted"))
                    .tid(convert(rs.getTimestamp("tid")))
                    .created(convert(rs.getTimestamp("created")));
        }
    }
}

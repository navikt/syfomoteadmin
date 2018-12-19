package no.nav.syfo.repository.dao;

import no.nav.syfo.repository.model.PEpost;
import no.nav.syfo.repository.model.PEpostVedlegg;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.nav.syfo.util.DbUtil.nesteSekvensverdi;

public class EpostDAO {
    @Inject
    private JdbcTemplate jdbcTemplate;
    @Inject
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<PEpost> finnEposterForSending() {
        return jdbcTemplate.query("select * from EPOST", new EpostMapper())
                .stream()
                .map(epost -> epost.vedlegg(finnVedleggTilEpost(epost.id)))
                .collect(toList());
    }

    public List<PEpostVedlegg> finnVedleggTilEpost(long epostId) {
        return jdbcTemplate.query("select * from EPOST_VEDLEGG where EPOST_ID = ?", new EpostVedleggMapper(), epostId);
    }

    @Transactional
    public void delete(long epostId) {
        jdbcTemplate.update("delete from epost_vedlegg where epost_id = ?", epostId);
        jdbcTemplate.update("delete from epost where epost_id = ?", epostId);
    }

    public long create(PEpost epost) {
        Long nesteSekvensverdi = nesteSekvensverdi("EPOST_ID_SEQ", jdbcTemplate);
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("epost_id", nesteSekvensverdi)
                .addValue("mottaker", epost.mottaker)
                .addValue("emne", epost.emne)
                .addValue("innhold", epost.innhold);
        namedParameterJdbcTemplate.update("insert into epost " +
                "(epost_id, mottaker, emne, innhold)" +
                "VALUES (:epost_id, :mottaker, :emne, :innhold)", namedParameters);
        return nesteSekvensverdi;
    }

    public long create(PEpostVedlegg epostVedlegg) {
        Long nesteSekvensverdi = nesteSekvensverdi("EPOST_VEDLEGG_ID_SEQ", jdbcTemplate);
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("epost_vedlegg_id", nesteSekvensverdi)
                .addValue("epost_id", epostVedlegg.epostId)
                .addValue("innhold", epostVedlegg.innhold)
                .addValue("type", epostVedlegg.type);
        namedParameterJdbcTemplate.update("insert into epost_vedlegg " +
                "(epost_vedlegg_id, epost_id, innhold, type)" +
                "VALUES (:epost_vedlegg_id, :epost_id, :innhold, :type)", namedParameters);
        return nesteSekvensverdi;
    }

    private class EpostMapper implements RowMapper<PEpost> {
        public PEpost mapRow(ResultSet rs, int rowNum) throws SQLException {
            jdbcTemplate.query("select * from EPOST_VEDLEGG where EPOST_ID = ?", new EpostVedleggMapper(), rs.getLong("epost_id"));
            return new PEpost()
                    .id(rs.getLong("epost_id"))
                    .mottaker(rs.getString("mottaker"))
                    .emne(rs.getString("emne"))
                    .innhold(rs.getString("innhold"));
        }
    }

    private class EpostVedleggMapper implements RowMapper<PEpostVedlegg> {
        public PEpostVedlegg mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PEpostVedlegg()
                    .id(rs.getLong("epost_vedlegg_id"))
                    .epostId(rs.getLong("epost_id"))
                    .innhold(rs.getString("innhold"))
                    .type(rs.getString("type"));
        }
    }

}

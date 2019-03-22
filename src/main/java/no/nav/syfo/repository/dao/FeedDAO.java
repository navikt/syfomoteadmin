package no.nav.syfo.repository.dao;

import no.nav.syfo.repository.model.PFeedHendelse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;
import static no.nav.syfo.util.DbUtil.convert;
import static no.nav.syfo.util.DbUtil.nesteSekvensverdi;

@Service
@Transactional
@Repository
public class FeedDAO {

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Inject
    public FeedDAO(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            JdbcTemplate jdbcTemplate
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PFeedHendelse> hendelserEtterTidspunkt(LocalDateTime timestamp) {
        return jdbcTemplate.query("select * from FEED where created > ?", new FeedHendelseMapper(), convert(timestamp));
    }

    public List<PFeedHendelse> finnFeedHendelserIMote(long moteId) {
        return jdbcTemplate.query("select * from FEED where mote_id = ?", new FeedHendelseMapper(), moteId);
    }

    public void createFeedHendelse(PFeedHendelse pFeedHendelse) {
        Long nesteSekvensverdi = nesteSekvensverdi("FEED_ID_SEQ", jdbcTemplate);
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("feed_id", nesteSekvensverdi)
                .addValue("uuid", pFeedHendelse.uuid)
                .addValue("created", convert(now()))
                .addValue("type", pFeedHendelse.type)
                .addValue("sist_endret_av", pFeedHendelse.sistEndretAv)
                .addValue("mote_id", pFeedHendelse.moteId);
        namedParameterJdbcTemplate.update("insert into feed " +
                "(feed_id, uuid, created, type, sist_endret_av, mote_id)" +
                "VALUES (:feed_id, :uuid, :created, :type, :sist_endret_av, :mote_id)", namedParameters);
    }

    public static class FeedHendelseMapper implements RowMapper<PFeedHendelse> {
        public PFeedHendelse mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PFeedHendelse()
                    .id(rs.getLong("feed_id"))
                    .uuid(rs.getString("uuid"))
                    .moteId(rs.getLong("mote_id"))
                    .created(convert(rs.getTimestamp("created")))
                    .sistEndretAv(rs.getString("sist_endret_av"))
                    .type(rs.getString("type"));
        }
    }

}


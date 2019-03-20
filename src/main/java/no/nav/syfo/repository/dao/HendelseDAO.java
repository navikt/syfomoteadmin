package no.nav.syfo.repository.dao;

import no.nav.syfo.domain.model.HendelseMoteStatusEndret;
import no.nav.syfo.domain.model.HendelsesType;
import no.nav.syfo.repository.model.PHendelseMoteStatusEndret;
import no.nav.syfo.repository.model.PHendelseVarselMotedeltaker;
import no.nav.syfo.repository.model.PHendelseVarselVeileder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static java.time.LocalDateTime.now;
import static no.nav.syfo.repository.mapper.PHendelseMapper.p2motestatusendrethendelse;
import static no.nav.syfo.util.DbUtil.convert;
import static no.nav.syfo.util.DbUtil.nesteSekvensverdi;
import static no.nav.syfo.util.MapUtil.mapListe;

@Service
@Transactional
@Repository
public class HendelseDAO {

    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public HendelseDAO(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            JdbcTemplate jdbcTemplate
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public long create(HendelseMoteStatusEndret hendelse) {
        Long nesteSekvensverdi = nesteSekvensverdi("HENDELSE_ID_SEQ", jdbcTemplate);
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("hendelse_id", nesteSekvensverdi)
                .addValue("inntruffetdato", convert(now()))
                .addValue("type", HendelsesType.MOTESTATUS_ENDRET.name())
                .addValue("opprettet_av", hendelse.opprettetAv)
                .addValue("status", hendelse.status.name())
                .addValue("mote_id", hendelse.moteId);
        namedParameterJdbcTemplate.update("insert into hendelse " +
                "(hendelse_id, inntruffetdato, type, opprettet_av)" +
                "VALUES (:hendelse_id, :inntruffetdato, :type, :opprettet_av)", namedParameters);
        namedParameterJdbcTemplate.update("insert into hendelse_motestatus_endret " +
                "(hendelse_id, status, mote_id)" +
                "VALUES (:hendelse_id, :status, :mote_id)", namedParameters);
        return nesteSekvensverdi;
    }

    public List<HendelseMoteStatusEndret> moteStatusEndretHendelser(long moteId) {
        return mapListe(jdbcTemplate.query("select * from hendelse_motestatus_endret join hendelse on hendelse.hendelse_id = hendelse_motestatus_endret.hendelse_id where mote_id = ?",
                new MoteStatusEndretHendelseMapper(), moteId), p2motestatusendrethendelse);
    }

    @Transactional
    public long create(PHendelseVarselMotedeltaker hendelse) {
        Long nesteSekvensverdi = nesteSekvensverdi("HENDELSE_ID_SEQ", jdbcTemplate);
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("hendelse_id", nesteSekvensverdi)
                .addValue("inntruffetdato", convert(now()))
                .addValue("type", HendelsesType.VARSEL.name())
                .addValue("hendelse_type", hendelse.type)
                .addValue("opprettet_av", hendelse.opprettetAv)
                .addValue("motedeltaker_id", hendelse.motedeltakerId)
                .addValue("resultat", hendelse.resultat)
                .addValue("adresse", hendelse.adresse)
                .addValue("kanal", hendelse.kanal)
                .addValue("varsel_type", hendelse.varseltype);
        namedParameterJdbcTemplate.update("insert into hendelse " +
                "(hendelse_id, inntruffetdato, type, opprettet_av)" +
                "VALUES (:hendelse_id, :inntruffetdato, :type, :opprettet_av)", namedParameters);
        namedParameterJdbcTemplate.update("insert into hendelse_varsel_motedeltaker " +
                "(hendelse_id, motedeltaker_id, resultat, adresse, kanal, varsel_type)" +
                "VALUES (:hendelse_id, :motedeltaker_id, :resultat, :adresse, :kanal, :varsel_type)", namedParameters);
        return nesteSekvensverdi;
    }

    @Transactional
    public long create(PHendelseVarselVeileder pHendelseVarselVeileder) {
        Long nesteSekvensverdi = nesteSekvensverdi("HENDELSE_ID_SEQ", jdbcTemplate);
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("hendelse_id", nesteSekvensverdi)
                .addValue("inntruffetdato", convert(now()))
                .addValue("hendelse_type", pHendelseVarselVeileder.type)
                .addValue("type", HendelsesType.VARSEL.name())
                .addValue("opprettet_av", pHendelseVarselVeileder.opprettetAv)
                .addValue("mote_id", pHendelseVarselVeileder.moteId)
                .addValue("veilederident", pHendelseVarselVeileder.veilederident)
                .addValue("kanal", pHendelseVarselVeileder.kanal)
                .addValue("varsel_type", pHendelseVarselVeileder.varseltype);
        namedParameterJdbcTemplate.update("insert into hendelse " +
                "(hendelse_id, inntruffetdato, type, opprettet_av)" +
                "VALUES (:hendelse_id, :inntruffetdato, :type, :opprettet_av)", namedParameters);
        namedParameterJdbcTemplate.update("insert into hendelse_varsel_veileder " +
                "(hendelse_id, mote_id, veilederident, kanal, varsel_type)" +
                "VALUES (:hendelse_id, :mote_id, :veilederident, :kanal, :varsel_type)", namedParameters);
        return nesteSekvensverdi;
    }

    @Transactional
    public void slettHendelseVarselVeileder(long moteId) {
        List<String> hendelseIder = jdbcTemplate.query("select * from hendelse_varsel_veileder " +
                "where mote_id = ?", (rs, rowNum) -> rs.getString("hendelse_id"), moteId);
        jdbcTemplate.update("delete from hendelse_varsel_veileder " +
                "where mote_id = ?", moteId);
        hendelseIder.forEach(hendelseId -> jdbcTemplate.update("delete from hendelse where hendelse_id = ?", hendelseId));
    }

    @Transactional
    public void slettHendelseMoteStatusEndret(long moteId) {
        List<String> hendelseIder = jdbcTemplate.query("select * from hendelse_motestatus_endret " +
                "where mote_id = ?", (rs, rowNum) -> rs.getString("hendelse_id"), moteId);
        jdbcTemplate.update("delete from hendelse_motestatus_endret " +
                "where mote_id = ?", moteId);
        hendelseIder.forEach(hendelseId -> jdbcTemplate.update("delete from hendelse where hendelse_id = ?", hendelseId));
    }

    @Transactional
    public void slettHendelseVarselMotedeltaker(long motedeltakerId) {
        List<String> hendelseIder = jdbcTemplate.query("select * from hendelse_varsel_motedeltaker " +
                "where motedeltaker_id = ?", (rs, rowNum) -> rs.getString("hendelse_id"), motedeltakerId);
        jdbcTemplate.update("delete from hendelse_varsel_motedeltaker " +
                "where motedeltaker_id = ?", motedeltakerId);
        hendelseIder.forEach(hendelseId -> jdbcTemplate.update("delete from hendelse where hendelse_id = ?", hendelseId));
    }


    public static class MoteStatusEndretHendelseMapper implements RowMapper<PHendelseMoteStatusEndret> {
        public PHendelseMoteStatusEndret mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PHendelseMoteStatusEndret()
                    .id(rs.getLong("hendelse_id"))
                    .status(rs.getString("status"))
                    .moteId(rs.getLong("mote_id"))
                    .inntruffetdato(convert(rs.getTimestamp("inntruffetdato")))
                    .type(rs.getString("type"))
                    .opprettetAv(rs.getString("opprettet_av"));
        }
    }
}

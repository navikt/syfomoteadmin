package no.nav.syfo.repository.dao;

import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.Motedeltaker;
import no.nav.syfo.domain.model.MotedeltakerAktorId;
import no.nav.syfo.domain.model.MotedeltakerArbeidsgiver;
import no.nav.syfo.repository.model.PMotedeltakerAktorId;
import no.nav.syfo.repository.model.PMotedeltakerArbeidsgiver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static no.nav.syfo.domain.model.MoteStatus.FLERE_TIDSPUNKT;
import static no.nav.syfo.domain.model.MoteStatus.OPPRETTET;
import static no.nav.syfo.domain.model.MoteStatus.AVBRUTT;
import static no.nav.syfo.domain.model.MoteStatus.BEKREFTET;
import static no.nav.syfo.domain.model.MotedeltakerStatus.SVART;
import static no.nav.syfo.repository.mapper.PMotedeltakerMapper.p2Aktoer;
import static no.nav.syfo.repository.mapper.PMotedeltakerMapper.p2Arbeidsgiver;
import static no.nav.syfo.util.DbUtil.convert;
import static no.nav.syfo.util.DbUtil.nesteSekvensverdi;
import static no.nav.syfo.util.MapUtil.map;
import static no.nav.syfo.util.MapUtil.mapListe;

public class MotedeltakerDAO {
    @Inject
    private TidOgStedDAO tidOgStedDAO;
    @Inject
    private MoteDAO moteDAO;
    @Inject
    private JdbcTemplate jdbcTemplate;
    @Inject
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Transactional
    public MotedeltakerAktorId create(PMotedeltakerAktorId motedeltaker) {
        Long nesteSekvensverdi = nesteSekvensverdi("MOTEDELTAKER_ID_SEQ", jdbcTemplate);
        String uuid = randomUUID().toString();

        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("motedeltaker_id", nesteSekvensverdi)
                .addValue("motedeltaker_uuid", uuid)
                .addValue("motedeltakertype", "Bruker")
                .addValue("mote_id", motedeltaker.moteId)
                .addValue("status", "OPPRETTET")
                .addValue("svar_tidspunkt", null)
                .addValue("aktor_id", motedeltaker.aktorId);
        namedParameterJdbcTemplate.update("insert into motedeltaker " +
                "(motedeltaker_id, motedeltaker_uuid, motedeltakertype, mote_id, status, svar_tidspunkt)" +
                "VALUES (:motedeltaker_id, :motedeltaker_uuid, :motedeltakertype, :mote_id, :status, :svar_tidspunkt)", namedParameters);
        namedParameterJdbcTemplate.update("insert into motedeltaker_aktorid " +
                "(motedeltaker_id, aktor_id)" +
                "VALUES (:motedeltaker_id, :aktor_id)", namedParameters);
        return map(motedeltaker.id(nesteSekvensverdi).uuid(uuid), p2Aktoer);
    }

    @Transactional
    public MotedeltakerArbeidsgiver create(PMotedeltakerArbeidsgiver motedeltaker) {
        Long nesteSekvensverdi = nesteSekvensverdi("MOTEDELTAKER_ID_SEQ", jdbcTemplate);
        String uuid = randomUUID().toString();
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("motedeltaker_id", nesteSekvensverdi)
                .addValue("motedeltaker_uuid", uuid)
                .addValue("motedeltakertype", "arbeidsgiver")
                .addValue("mote_id", motedeltaker.moteId)
                .addValue("status", "OPPRETTET")
                .addValue("svar_tidspunkt", null)
                .addValue("navn", motedeltaker.navn)
                .addValue("epost", motedeltaker.epost)
                .addValue("orgnummer", motedeltaker.orgnummer);
        namedParameterJdbcTemplate.update("insert into motedeltaker " +
                "(motedeltaker_id, motedeltaker_uuid, motedeltakertype, mote_id, status, svar_tidspunkt) " +
                "VALUES (:motedeltaker_id, :motedeltaker_uuid, :motedeltakertype, :mote_id, :status, :svar_tidspunkt)", namedParameters);
        namedParameterJdbcTemplate.update("insert into motedeltaker_arbeidsgiver " +
                "(motedeltaker_id, navn, epost, orgnummer) " +
                "VALUES (:motedeltaker_id, :navn, :epost, :orgnummer)", namedParameters);
        return map(motedeltaker.id(nesteSekvensverdi).uuid(uuid), p2Arbeidsgiver);
    }

    public void motedeltakerHarSvart(String motedeltakerUuid, List<Long> valgteAlternativer) {
        long motedeltakerId = jdbcTemplate.queryForObject("select * from motedeltaker where motedeltaker_uuid = ?", (rs, rowNum) -> rs.getLong("motedeltaker_id"), motedeltakerUuid);
        valgteAlternativer.forEach(tidOgStedId -> jdbcTemplate.update("insert into motedeltaker_tid_sted values(?, ?)", motedeltakerId, tidOgStedId));

        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("motedeltaker_uuid", motedeltakerUuid)
                .addValue("status", SVART.name())
                .addValue("svar_tidspunkt", convert(LocalDateTime.now()));
        namedParameterJdbcTemplate.update("update motedeltaker " +
                "set status = :status, " +
                "svar_tidspunkt = :svar_tidspunkt " +
                "where motedeltaker_uuid = :motedeltaker_uuid", namedParameters);
    }

    public List<String> sykmeldteMedMoteHvorBeggeHarSvart(String enhet) {
        return jdbcTemplate.query("select distinct AKTOR_ID " +
                        "from MOTEDELTAKER ag " +
                        "join MOTEDELTAKER sm " +
                        "on ag.MOTE_ID = sm.MOTE_ID " +
                        "join MOTEDELTAKER_AKTORID " +
                        "on sm.MOTEDELTAKER_ID = MOTEDELTAKER_AKTORID.MOTEDELTAKER_ID " +
                        "where ag.MOTEDELTAKERTYPE = 'arbeidsgiver' " +
                        "and sm.MOTEDELTAKERTYPE = 'Bruker' " +
                        "and ag.STATUS = '" + SVART.name() + "' " +
                        "and sm.STATUS = '" + SVART.name() + "' " +
                        "and sm.MOTE_ID in (select MOTE_ID from MOTE where STATUS not in ('" + AVBRUTT.name() + "','" + BEKREFTET.name() + "') and NAV_ENHET = ?)",
                new MotedeltakerAktorIdMapper(), enhet);
    }

    public List<Motedeltaker> motedeltakereByMoteId(long moteId) {
        MotedeltakerArbeidsgiver arbeidsgiver = map(jdbcTemplate.queryForObject("select * from motedeltaker_arbeidsgiver " +
                        "inner join motedeltaker on motedeltaker.motedeltaker_id = motedeltaker_arbeidsgiver.motedeltaker_id " +
                        "where mote_id = ? and motedeltakertype = 'arbeidsgiver'",
                new MotedeltakerArbeidsgiverMapper(), moteId), p2Arbeidsgiver);
        arbeidsgiver.tidOgStedAlternativer = tidOgStedDAO.finnAlternativerMedBrukersValg(moteId, arbeidsgiver.id);

        MotedeltakerAktorId sykmeldt = map(jdbcTemplate.queryForObject("select * from motedeltaker_aktorid " +
                "inner join motedeltaker on motedeltaker.motedeltaker_id = motedeltaker_aktorid.motedeltaker_id " +
                "where mote_id = ? and motedeltakertype = 'Bruker'", new MotedeltakerAktorMapper(), moteId), p2Aktoer);
        sykmeldt.tidOgStedAlternativer(tidOgStedDAO.finnAlternativerMedBrukersValg(moteId, sykmeldt.id));

        List<Motedeltaker> motedeltakere = new ArrayList<>();
        motedeltakere.add(arbeidsgiver);
        motedeltakere.add(sykmeldt);
        return motedeltakere;
    }

    public MotedeltakerAktorId arbeidstakerMotedeltakerAktorIdByMoteId(long moteId) {
        return map(jdbcTemplate.queryForObject("select * from motedeltaker_aktorid " +
                "inner join motedeltaker on motedeltaker.motedeltaker_id = motedeltaker_aktorid.motedeltaker_id " +
                "where mote_id = ? and motedeltakertype = 'Bruker'", new MotedeltakerAktorMapper(), moteId), p2Aktoer);
    }

    public List<MotedeltakerArbeidsgiver> findMotedeltakereSomIkkeHarSvartSisteDognet(int antallDagerBakoverEkstra) {
        List<MotedeltakerArbeidsgiver> arbeidstakere = mapListe(jdbcTemplate.query("select * from motedeltaker_arbeidsgiver inner join motedeltaker " +
                        "on motedeltaker.motedeltaker_id = motedeltaker_arbeidsgiver.motedeltaker_id " +
                        "where (status = ? or status = ?) and svar_tidspunkt is null",
                new MotedeltakerArbeidsgiverMapper(),
                OPPRETTET.name(), FLERE_TIDSPUNKT.name()), p2Arbeidsgiver);

        return arbeidstakere.stream().filter(arbeidsgiver -> {
            Mote Mote = moteDAO.findMoteByMotedeltakerUuid(arbeidsgiver.uuid);
            return Mote.opprettetTidspunkt.isAfter(now().atStartOfDay().minusDays(1 + antallDagerBakoverEkstra)) && Mote.opprettetTidspunkt.isBefore(now().atStartOfDay());
        }).collect(toList());
    }

    @Transactional
    public void slettSykmeldtById(long motedeltakerId) {
        jdbcTemplate.update("delete from motedeltaker_aktorid " +
                "where motedeltaker_id = ?", motedeltakerId);
        jdbcTemplate.update("delete from motedeltaker " +
                "where motedeltaker_id = ?", motedeltakerId);
    }

    @Transactional
    public void slettArbeidsgiverById(long motedeltakerId) {
        jdbcTemplate.update("delete from motedeltaker_arbeidsgiver " +
                "where motedeltaker_id = ?", motedeltakerId);
        jdbcTemplate.update("delete from motedeltaker " +
                "where motedeltaker_id = ?", motedeltakerId);
    }

    public static class MotedeltakerAktorMapper implements RowMapper<PMotedeltakerAktorId> {
        public PMotedeltakerAktorId mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PMotedeltakerAktorId()
                    .id(rs.getLong("motedeltaker_id"))
                    .moteId(rs.getLong("mote_id"))
                    .uuid(rs.getString("motedeltaker_uuid"))
                    .motedeltakertype(rs.getString("motedeltakertype"))
                    .status(rs.getString("status"))
                    .svarTidspunkt(convert(rs.getTimestamp("svar_tidspunkt")))
                    .aktorId(rs.getString("aktor_id"));
        }
    }

    public static class MotedeltakerArbeidsgiverMapper implements RowMapper<PMotedeltakerArbeidsgiver> {
        public PMotedeltakerArbeidsgiver mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PMotedeltakerArbeidsgiver()
                    .id(rs.getLong("motedeltaker_id"))
                    .moteId(rs.getLong("mote_id"))
                    .uuid(rs.getString("motedeltaker_uuid"))
                    .motedeltakertype(rs.getString("motedeltakertype"))
                    .status(rs.getString("status"))
                    .svarTidspunkt(convert(rs.getTimestamp("svar_tidspunkt")))
                    .navn(rs.getString("navn"))
                    .epost(rs.getString("epost"))
                    .orgnummer(rs.getString("orgnummer"));
        }
    }

    public static class MotedeltakerAktorIdMapper implements RowMapper<String> {
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("aktor_id");
        }
    }
}

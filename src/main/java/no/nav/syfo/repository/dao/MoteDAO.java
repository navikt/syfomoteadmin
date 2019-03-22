package no.nav.syfo.repository.dao;

import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.repository.model.PMotedeltakerAktorId;
import no.nav.syfo.repository.model.PMotedeltakerArbeidsgiver;
import org.springframework.context.annotation.Lazy;
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
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static no.nav.syfo.domain.model.MoteStatus.BEKREFTET;
import static no.nav.syfo.repository.mapper.PMoteMapper.p2Mote;
import static no.nav.syfo.util.DbUtil.convert;
import static no.nav.syfo.util.DbUtil.nesteSekvensverdi;
import static no.nav.syfo.util.MapUtil.map;

@Service
@Repository
public class MoteDAO {

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private MotedeltakerDAO motedeltakerDAO;
    private TidOgStedDAO tidOgStedDAO;

    @Inject
    public MoteDAO(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            JdbcTemplate jdbcTemplate,
            @Lazy MotedeltakerDAO motedeltakerDAO,
            TidOgStedDAO tidOgStedDAO
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.motedeltakerDAO = motedeltakerDAO;
        this.tidOgStedDAO = tidOgStedDAO;
    }

    public Mote create(Mote Mote) {
        Long nesteSekvensverdi = nesteSekvensverdi("MOTE_ID_SEQ", jdbcTemplate);
        String uuid = randomUUID().toString();
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("mote_id", nesteSekvensverdi)
                .addValue("mote_uuid", uuid)
                .addValue("opprettet_av", Mote.opprettetAv)
                .addValue("nav_enhet", Mote.navEnhet)
                .addValue("created", convert(now()))
                .addValue("valgt_tid_sted_id", null)
                .addValue("status", "OPPRETTET")
                .addValue("eier", Mote.eier);
        namedParameterJdbcTemplate.update("insert into mote " +
                "(mote_id, mote_uuid, opprettet_av, nav_enhet, created, valgt_tid_sted_id, status, eier)" +
                "VALUES (:mote_id, :mote_uuid, :opprettet_av, :nav_enhet, :created, :valgt_tid_sted_id, :status, :eier)", namedParameters);
        return Mote.id(nesteSekvensverdi).uuid(uuid);
    }

    public void setStatus(long moteId, String status) {
        jdbcTemplate.update("update mote " +
                "set status = ? " +
                "where mote_id = ?", status, moteId);
    }

    public void bekreftMote(long moteId, long valgtTidOgStedId) {
        jdbcTemplate.update("update mote " +
                "set status = ?, " +
                "valgt_tid_sted_id = ? " +
                "where mote_id = ?", BEKREFTET.name(), valgtTidOgStedId, moteId);
    }

    public Mote findMoteByUUID(String uuid) {
        return tilMote(jdbcTemplate.queryForObject("select * from mote where mote_uuid = ?", new MoteMapper(), uuid));
    }

    public Mote findMoteByID(long id) {
        return tilMote(jdbcTemplate.queryForObject("select * from mote where mote_id = ?", new MoteMapper(), id));
    }

    public List<Mote> findMoterByBrukerAktoerId(String aktorId) {
        return tilMote(jdbcTemplate.query("select * from mote " +
                "left join motedeltaker ON mote.mote_id = motedeltaker.mote_id " +
                "left join motedeltaker_aktorid on motedeltaker_aktorid.motedeltaker_id = motedeltaker.motedeltaker_id " +
                "where aktor_id = ?", new MoteMapper(), aktorId));
    }

    public List<Mote> findMoterByNavAnsatt(String navansatt) {
        return tilMote(jdbcTemplate.query("select * from mote where eier = ? and status !='AVBRUTT'", new MoteMapper(), navansatt));
    }

    public List<Mote> findMoterByNavEnhet(String navenhet) {
        return tilMote(jdbcTemplate.query("select * from mote where nav_enhet = ? and status !='AVBRUTT'", new MoteMapper(), navenhet));
    }

    //TODO dette bør kunne gjøres bedre...
    public List<Mote> findMoterByBrukerAktoerIdOgAGOrgnummer(String aktorId, String orgnummer) {
        List<PMotedeltakerAktorId> deltakereByAktoerId = jdbcTemplate.query("select * from motedeltaker " +
                "left join motedeltaker_aktorid on motedeltaker_aktorid.motedeltaker_id = motedeltaker.motedeltaker_id " +
                "where aktor_id = ?", new MotedeltakerDAO.MotedeltakerAktorMapper(), aktorId);
        List<PMotedeltakerArbeidsgiver> deltakereByOrgnummer  = jdbcTemplate.query("select * from motedeltaker " +
                "left join motedeltaker_arbeidsgiver on motedeltaker_arbeidsgiver.motedeltaker_id = motedeltaker.motedeltaker_id " +
                "where orgnummer = ?", new MotedeltakerDAO.MotedeltakerArbeidsgiverMapper(), orgnummer);

        return deltakereByAktoerId.stream()
                .filter(aktoer -> deltakereByOrgnummer.stream().anyMatch(arbeidsgiver -> arbeidsgiver.moteId == aktoer.moteId))
                .map(aktoer -> tilMote(jdbcTemplate.queryForObject("select * from mote " +
                        "where mote_id = ?", new MoteMapper(), aktoer.moteId)))
                .collect(toList());
    }

    private Mote tilMote(no.nav.syfo.repository.model.PMote pPMote) {
        return map(pPMote, p2Mote)
                .alternativer(tidOgStedDAO.finnAlternativer(pPMote.id))
                .valgtTidOgSted(pPMote.valgtTidStedId != 0 ? tidOgStedDAO.finnAlternativ(pPMote.valgtTidStedId) : null)
                .motedeltakere(motedeltakerDAO.motedeltakereByMoteId(pPMote.id));
    }

    private List<Mote> tilMote(List<no.nav.syfo.repository.model.PMote> pMoter) {
        return pMoter.stream()
                .map(this::tilMote)
                .collect(toList());
    }

    public Mote findMoteByMotedeltakerUuid(String uuid) {
        no.nav.syfo.repository.model.PMote pPMote = jdbcTemplate.queryForObject("select * from mote " +
                "left join motedeltaker ON mote.mote_id = motedeltaker.mote_id " +
                "where motedeltaker.motedeltaker_uuid = ?", new MoteMapper(), uuid);
        return tilMote(pPMote);
    }

    public List<Mote> finnMoterOpprettetSisteMnd() {
        return tilMote(jdbcTemplate.query("select * from mote " +
                "where created > ?", new MoteMapper(), convert(now().minusMonths(1))));
    }

    public List<Mote> harSvarNyereEnnTimestamp(LocalDateTime timestamp) {
        return tilMote(jdbcTemplate.query("select * from mote join motedeltaker " +
                "on mote.MOTE_ID = motedeltaker.MOTE_ID " +
                "where motedeltaker.SVAR_TIDSPUNKT > ?", new MoteMapper(), timestamp));
    }

    public void oppdaterMoteEier(String moteUuid, String mottakerUserId) {
        jdbcTemplate.update("update mote " +
                "set eier = ? " +
                "where mote_uuid = ?", mottakerUserId, moteUuid);
    }

    public static class MoteMapper implements RowMapper<no.nav.syfo.repository.model.PMote> {
        public no.nav.syfo.repository.model.PMote mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new no.nav.syfo.repository.model.PMote()
                    .id(rs.getLong("mote_id"))
                    .uuid(rs.getString("mote_uuid"))
                    .opprettetAv(rs.getString("opprettet_av"))
                    .navEnhet(rs.getString("nav_enhet"))
                    .created(convert(rs.getTimestamp("created")))
                    .valgtTidStedId(rs.getLong("valgt_tid_sted_id"))
                    .status(rs.getString("status"))
                    .eier(rs.getString("eier"));
        }
    }
}

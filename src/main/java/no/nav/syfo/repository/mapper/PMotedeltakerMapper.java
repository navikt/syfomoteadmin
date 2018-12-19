package no.nav.syfo.repository.mapper;

import no.nav.syfo.domain.model.MotedeltakerAktorId;
import no.nav.syfo.domain.model.MotedeltakerArbeidsgiver;
import no.nav.syfo.domain.model.MotedeltakerStatus;
import no.nav.syfo.repository.model.PMotedeltakerAktorId;
import no.nav.syfo.repository.model.PMotedeltakerArbeidsgiver;

import java.util.function.Function;

public class PMotedeltakerMapper {

    public static Function<PMotedeltakerArbeidsgiver, MotedeltakerArbeidsgiver> p2Arbeidsgiver = pMotedeltaker ->
            new MotedeltakerArbeidsgiver()
                    .id(pMotedeltaker.id)
                    .status(MotedeltakerStatus.valueOf(pMotedeltaker.status))
                    .motedeltakertype("arbeidsgiver")
                    .svartTidspunkt(pMotedeltaker.svarTidspunkt)
                    .uuid(pMotedeltaker.uuid)
                    .epost(pMotedeltaker.epost)
                    .orgnummer(pMotedeltaker.orgnummer)
                    .navn(pMotedeltaker.navn);

    public static Function<PMotedeltakerAktorId, MotedeltakerAktorId> p2Aktoer = pMotedeltaker ->
            new MotedeltakerAktorId()
                    .id(pMotedeltaker.id)
                    .uuid(pMotedeltaker.uuid)
                    .status(MotedeltakerStatus.valueOf(pMotedeltaker.status))
                    .motedeltakertype("Bruker")
                    .svartTidspunkt(pMotedeltaker.svarTidspunkt)
                    .uuid(pMotedeltaker.uuid)
                    .aktorId(pMotedeltaker.aktorId);

}

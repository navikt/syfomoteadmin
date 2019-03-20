package no.nav.syfo.api.mappers;

import no.nav.syfo.domain.model.Mote;
import no.nav.syfo.domain.model.MotedeltakerAktorId;
import no.nav.syfo.domain.model.MotedeltakerArbeidsgiver;
import no.nav.syfo.domain.model.TidOgSted;
import no.nav.syfo.api.domain.nyttmoterequest.RSNyttAlternativ;
import no.nav.syfo.api.domain.nyttmoterequest.RSNyttMoteRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.function.Function;

import static no.nav.syfo.domain.model.MoteStatus.OPPRETTET;
import static no.nav.syfo.util.MapUtil.mapListe;

public class RSNyttMoteMapper {
    public static Function<RSNyttAlternativ, TidOgSted> opprett2TidOgSted = rsTidOgSted ->
            new TidOgSted()
                    .tid(LocalDateTime.parse(rsTidOgSted.tid, DateTimeFormatter.ISO_DATE_TIME))
                    .sted(rsTidOgSted.sted);

    public static Function<RSNyttMoteRequest, Mote> opprett2Mote = nyttMoteRequest ->
            new Mote()
                    .navEnhet(nyttMoteRequest.navEnhet)
                    .status(OPPRETTET)
                    .motedeltakere(Arrays.asList(
                            new MotedeltakerArbeidsgiver().orgnummer(nyttMoteRequest.orgnummer).epost(nyttMoteRequest.epost).navn(nyttMoteRequest.navn),
                            new MotedeltakerAktorId().fnr(nyttMoteRequest.fnr).motedeltakertype("Bruker")
                    ))
                    .alternativer(mapListe(nyttMoteRequest.alternativer, opprett2TidOgSted));

}

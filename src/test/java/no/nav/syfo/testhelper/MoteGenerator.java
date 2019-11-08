package no.nav.syfo.testhelper;

import no.nav.syfo.api.ressurser.azuread.EmailContentController;
import no.nav.syfo.domain.model.*;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID;

public class MoteGenerator {

    private final TidOgSted tidOgSted = new TidOgSted()
            .id(1L)
            .tid(LocalDateTime.now().plusMonths(1L))
            .sted("Nav");

    private final Motedeltaker moteDeltaker = new MotedeltakerAktorId()
            .uuid(UUID.randomUUID().toString())
            .aktorId(ARBEIDSTAKER_AKTORID)
            .motedeltakertype(EmailContentController.BRUKER)
            .tidOgStedAlternativer(singletonList(tidOgSted));

    private final Mote mote = new Mote()
            .motedeltakere(singletonList(moteDeltaker));

    public Mote generateMote() {
        return mote;
    }

    public Mote generateMote(UUID uuid) {
        return mote
                .motedeltakere(singletonList(moteDeltaker.uuid(uuid.toString())));
    }
}

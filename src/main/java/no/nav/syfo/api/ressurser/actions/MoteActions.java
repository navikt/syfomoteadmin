package no.nav.syfo.api.ressurser.actions;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.api.domain.nyttmoterequest.RSNyttAlternativ;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.ArenaService;
import no.nav.syfo.service.MoteService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

import static no.nav.syfo.api.mappers.RSNyttMoteMapper.opprett2TidOgSted;
import static no.nav.syfo.oidc.OIDCIssuer.INTERN;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.OIDCUtil.getSubjectIntern;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/moter/{moteUuid}")
@ProtectedWithClaims(issuer = INTERN)
public class MoteActions {

    private OIDCRequestContextHolder contextHolder;

    private Metrikk metrikk;

    private MoteService moteService;

    private ArenaService arenaService;

    @Inject
    public MoteActions(
            OIDCRequestContextHolder contextHolder,
            Metrikk metrikk,
            MoteService moteService,
            ArenaService arenaService
    ) {
        this.contextHolder = contextHolder;
        this.metrikk = metrikk;
        this.moteService = moteService;
        this.arenaService = arenaService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/avbryt")
    public void avbryt(@PathVariable("moteUuid") String moteUuid, @RequestParam(value = "varsle") boolean varsle) {
        moteService.avbrytMote(moteUuid, varsle);

        metrikk.tellEndepunktKall("avbryt_mote");
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/bekreft")
    public void bekreft(@PathVariable("moteUuid") String moteUuid, @RequestParam(value = "valgtAlternativId") Long tidOgStedId) {
        moteService.bekreftMote(moteUuid, tidOgStedId);

        metrikk.tellEndepunktKall("bekreft_mote");
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/nyealternativer")
    public void nyeAlternativer(@PathVariable("moteUuid") String moteUuid, @RequestBody List<RSNyttAlternativ> alternativer) {
        moteService.nyeAlternativer(moteUuid, mapListe(alternativer, opprett2TidOgSted));

        metrikk.tellEndepunktKall("nye_alternativer");
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/opprettSanksjonsoppgave")
    public void opprettSanksjonsoppgave(@PathVariable("moteUuid") String moteUuid) {
        arenaService.bestillOppgave(moteUuid);
    }
}

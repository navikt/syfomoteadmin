package no.nav.syfo.api.ressurser;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.LocalApplication;
import no.nav.syfo.api.domain.RSMote;
import no.nav.syfo.api.domain.nyttmoterequest.RSNyttMoteRequest;
import no.nav.syfo.domain.model.*;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.dao.MotedeltakerDAO;
import no.nav.syfo.repository.dao.TidOgStedDAO;
import no.nav.syfo.repository.model.PMotedeltakerAktorId;
import no.nav.syfo.repository.model.PMotedeltakerArbeidsgiver;
import no.nav.syfo.service.*;
import no.nav.syfo.service.varselinnhold.ArbeidsgiverVarselService;
import no.nav.syfo.service.varselinnhold.SykmeldtVarselService;
import no.nav.syfo.sts.StsConsumer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static no.nav.syfo.controller.testhelper.RestHelperKt.mockAndExpectBehandlendeEnhetRequest;
import static no.nav.syfo.controller.testhelper.RestHelperKt.mockAndExpectSTSService;
import static no.nav.syfo.testhelper.OidcTestHelper.lagOIDCValidationContextIntern;
import static no.nav.syfo.testhelper.UserConstants.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class MoterRessursTilgangTest {

    private static final String AKTOER_ID_2 = "1101010101010";
    private static final String FNR_2 = "11010101010";
    private static final String NAVENHET = "1234";
    private static final TpsPerson skjermet_tpsPerson = new TpsPerson().skjermetBruker(true);
    private static final TpsPerson tpsPerson = new TpsPerson().skjermetBruker(false);

    @Value("${syfobehandlendeenhet.url}")
    private String behandlendeenhetUrl;
    @Value("${security.token.service.rest.url}")
    private String stsUrl;
    @Value("${srv.username}")
    private String srvUsername;
    @Value("${srv.password}")
    private String srvPassword;

    @MockBean
    public OIDCRequestContextHolder oidcRequestContextHolder;
    @MockBean
    private AktoerService aktoerService;
    @MockBean
    private MoteService moteService;
    @MockBean
    private Metrikk metrikk;
    @MockBean
    private TidOgStedDAO tidOgStedDAO;
    @MockBean
    private HendelseService hendelseService;
    @MockBean
    private MotedeltakerDAO motedeltakerDAO;
    @MockBean
    private NorgService norgService;
    @MockBean
    private BrukerprofilService brukerprofilService;
    @MockBean
    private VeilederService veilederService;
    @MockBean
    private ArbeidsgiverVarselService arbeidsgiverVarselService;
    @MockBean
    private SykefravaersoppfoelgingService sykefravaersoppfoelgingService;
    @MockBean
    private SykmeldtVarselService sykmeldtVarselService;
    @MockBean
    private TilgangService tilgangService;

    @Inject
    private CacheManager cacheManager;
    @Inject
    private StsConsumer stsConsumer;
    @Inject
    private RestTemplate restTemplate;

    @Inject
    private MoterRessurs moterRessurs;

    private MockRestServiceServer mockRestServiceServer;

    @Before
    public void setUp() {
        this.mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();
        when(oidcRequestContextHolder.getOIDCValidationContext()).thenReturn(lagOIDCValidationContextIntern(VEILEDER_ID));
        when(aktoerService.hentFnrForAktoer(ARBEIDSTAKER_AKTORID)).thenReturn(ARBEIDSTAKER_FNR);
        when(aktoerService.hentAktoerIdForIdent(ARBEIDSTAKER_FNR)).thenReturn(ARBEIDSTAKER_AKTORID);
        when(aktoerService.hentFnrForAktoer(AKTOER_ID_2)).thenReturn(FNR_2);
        when(moteService.findMoterByBrukerNavEnhet(NAVENHET)).thenReturn(MoteList);
    }

    @After
    public void cleanUp() {
        mockRestServiceServer.reset();
        cacheManager.getCacheNames()
                .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }

    private List<Mote> MoteList = asList(
            new Mote()
                    .id(1337L)
                    .status(MoteStatus.OPPRETTET)
                    .opprettetTidspunkt(now().minusMonths(1L))
                    .motedeltakere(singletonList(new MotedeltakerAktorId().aktorId(ARBEIDSTAKER_AKTORID))),
            new Mote()
                    .id(1338L)
                    .status(MoteStatus.OPPRETTET)
                    .opprettetTidspunkt(now().minusMonths(2L))
                    .motedeltakere(singletonList(new MotedeltakerAktorId().aktorId(AKTOER_ID_2)))
    );

    @Test
    public void hentMoter_fnr_veileder_har_tilgang() {
        Mote Mote1 = new Mote()
                .id(1337L)
                .status(MoteStatus.OPPRETTET)
                .opprettetTidspunkt(now().minusMonths(1L))
                .motedeltakere(singletonList(new MotedeltakerAktorId().aktorId(ARBEIDSTAKER_AKTORID)));
        Mote Mote2 = new Mote()
                .id(1338L)
                .status(MoteStatus.OPPRETTET)
                .opprettetTidspunkt(now().minusMonths(2L))
                .motedeltakere(singletonList(new MotedeltakerAktorId().aktorId(ARBEIDSTAKER_AKTORID)));

        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(true);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);
        when(moteService.findMoterByBrukerAktoerId(ARBEIDSTAKER_AKTORID)).thenReturn(asList(Mote1, Mote2));
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterRessurs.hentMoter(null, ARBEIDSTAKER_FNR, false, null, false);

        assertEquals(ARBEIDSTAKER_AKTORID, moteList.get(0).aktorId);
        assertEquals(ARBEIDSTAKER_AKTORID, moteList.get(1).aktorId);

        verify(brukerprofilService, times(3)).hentBruker(ARBEIDSTAKER_FNR);
        verify(aktoerService, times(4)).hentFnrForAktoer(ARBEIDSTAKER_AKTORID);
        verify(aktoerService, times(1)).hentAktoerIdForIdent(ARBEIDSTAKER_FNR);
        verify(moteService).findMoterByBrukerAktoerId(ARBEIDSTAKER_AKTORID);
    }

    @Test(expected = ForbiddenException.class)
    public void hentMoter_fnr_veileder_har_ikke_tilgang_pga_rolle() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(false);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);

        moterRessurs.hentMoter(null, ARBEIDSTAKER_FNR, false, null, false);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
    }

    @Test(expected = ForbiddenException.class)
    public void hentMoter_fnr_veileder_har_ikke_tilgang_pga_skjermet_bruker() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(true);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(skjermet_tpsPerson);

        moterRessurs.hentMoter(null, ARBEIDSTAKER_FNR, false, null, false);
    }

    @Test(expected = RuntimeException.class)
    public void hentMoter_fnr_veileder_annen_tilgangsfeil() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(false);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);

        moterRessurs.hentMoter(null, ARBEIDSTAKER_FNR, false, null, false);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
    }

    @Test
    public void hentMoter_navenhet_veileder_har_full_tilgang() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(true);
        when(tilgangService.harVeilederTilgangTilPerson(FNR_2)).thenReturn(true);

        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR))
                .thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2))
                .thenReturn(tpsPerson);
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterRessurs.hentMoter(null, null, false, NAVENHET, false);

        assertEquals(2, moteList.size());
        assertEquals(ARBEIDSTAKER_AKTORID, moteList.get(0).aktorId);
        assertEquals(AKTOER_ID_2, moteList.get(1).aktorId);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test
    public void hentMoter_navenhet_veileder_har_delvis_tilgang_pga_rolle() {
        when(tilgangService.harVeilederTilgangTilPerson(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR))
                .thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2))
                .thenReturn(tpsPerson);
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterRessurs.hentMoter(null, null, false, NAVENHET, false);

        assertEquals(1, moteList.size());
        assertEquals(ARBEIDSTAKER_AKTORID, moteList.get(0).aktorId);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test
    public void hentMoter_navenhet_veileder_har_delvis_tilgang_pga_skjermet_bruker() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(true);

        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR))
                .thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2))
                .thenReturn(skjermet_tpsPerson);

        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterRessurs.hentMoter(null, null, false, NAVENHET, false);

        assertEquals(1, moteList.size());
        assertEquals(ARBEIDSTAKER_AKTORID, moteList.get(0).aktorId);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test
    public void hentMoter_navenhet_veileder_har_ikke_tilgang_pga_rolle() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(false);

        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2)).thenReturn(tpsPerson);

        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterRessurs.hentMoter(null, null, false, NAVENHET, false);

        assertEquals(0, moteList.size());

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test
    public void hentMoter_navenhet_veileder_har_ikke_tilgang_pga_skjerming() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(true);

        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(skjermet_tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2)).thenReturn(skjermet_tpsPerson);
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        List<RSMote> moteList = moterRessurs.hentMoter(null, null, false, NAVENHET, false);

        assertEquals(0, moteList.size());

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test(expected = RuntimeException.class)
    public void hentMoter_navenhet_annen_tilgangsfeil() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(true);
        doThrow(new RuntimeException()).when(tilgangService).harVeilederTilgangTilPerson(FNR_2);

        when(norgService.hoererNavEnhetTilBruker(anyString(), anyString())).thenReturn(true);
        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);
        when(brukerprofilService.hentBruker(FNR_2)).thenReturn(tpsPerson);
        when(hendelseService.sistEndretMoteStatus(anyLong())).thenReturn(empty());

        moterRessurs.hentMoter(null, null, false, NAVENHET, false);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
        verify(brukerprofilService).hentBruker(FNR_2);
    }

    @Test
    public void opprettMoter_har_tilgang() {
        final RSNyttMoteRequest nyttMoteRequest = new RSNyttMoteRequest()
                .fnr(ARBEIDSTAKER_FNR)
                .orgnummer("123");

        mockBehandlendEnhet(ARBEIDSTAKER_FNR);

        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(true);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);
        when(sykefravaersoppfoelgingService.hentNaermesteLederSomBruker(ARBEIDSTAKER_AKTORID, nyttMoteRequest.orgnummer)).thenReturn(
                new NaermesteLeder()
                        .navn("Frida Frisk")
                        .epost("frida.frisk@bedrift.no")
        );
        when(moteService.opprettMote(any())).thenReturn(new Mote().id(1L));
        when(tidOgStedDAO.create(any())).thenReturn(new TidOgSted());
        when(motedeltakerDAO.create(any(PMotedeltakerAktorId.class))).thenReturn(new MotedeltakerAktorId());
        when(motedeltakerDAO.create(any(PMotedeltakerArbeidsgiver.class))).thenReturn(new MotedeltakerArbeidsgiver());
        when(veilederService.hentVeileder(any())).thenReturn(new Veileder());

        moterRessurs.opprett(nyttMoteRequest);

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
    }

    @Test(expected = ForbiddenException.class)
    public void opprettMoter_ikke_tilgang_pga_skjermet_bruker() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(true);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(skjermet_tpsPerson);

        moterRessurs.opprett(new RSNyttMoteRequest().fnr(ARBEIDSTAKER_FNR));

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
    }

    @Test(expected = ForbiddenException.class)
    public void opprettMoter_ikke_tilgang_pga_rolle() {
        when(tilgangService.harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR)).thenReturn(false);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);

        moterRessurs.opprett(new RSNyttMoteRequest().fnr(ARBEIDSTAKER_FNR));

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
    }

    @Test(expected = RuntimeException.class)
    public void opprettMoter_annen_tilgangsfeil() {
        doThrow(new RuntimeException()).when(tilgangService).harVeilederTilgangTilPerson(ARBEIDSTAKER_FNR);

        when(brukerprofilService.hentBruker(ARBEIDSTAKER_FNR)).thenReturn(tpsPerson);

        moterRessurs.opprett(new RSNyttMoteRequest().fnr(ARBEIDSTAKER_FNR));

        verify(brukerprofilService).hentBruker(ARBEIDSTAKER_FNR);
    }


    private void mockSTS() {
        if (!stsConsumer.isTokenCached()) {
            mockAndExpectSTSService(mockRestServiceServer, stsUrl, srvUsername, srvPassword);
        }
    }

    private void mockBehandlendEnhet(String fnr) {
        mockSTS();
        mockAndExpectBehandlendeEnhetRequest(mockRestServiceServer, behandlendeenhetUrl, fnr);
    }
}

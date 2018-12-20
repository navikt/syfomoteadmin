package no.nav.syfo.api.ressurser;

import no.nav.brukerdialog.security.context.SubjectRule;
import no.nav.brukerdialog.security.domain.IdentType;
import no.nav.common.auth.SsoToken;
import no.nav.common.auth.Subject;
import no.nav.syfo.service.TilgangService;
import org.glassfish.jersey.message.internal.Statuses;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Hensikten her er å samle koden som mock svar fra syfo-tilgangskontroll.
 * Subklasser arver tilgangskontrollResponse, som de kan sette opp til å returnere 200 OK, 403 Forbidden eller
 * 500-feil.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientBuilder.class)
public abstract class AbstractRessursTilgangTest {

    static final String FNR = "12345678910";
    static final String AKTOER_ID = "123456789";
    static final Response.StatusType TAU_I_PROPELLEN = Statuses.from(500, "Tau i propellen");

    private static Client client;
    private static TilgangService tilgangService;
    private static final SsoToken SSO_TOKEN = SsoToken.oidcToken("TestToken");

    @Mock
    Response tilgangskontrollResponse;

    @Rule
    public SubjectRule subjectRule = new SubjectRule();

    @BeforeClass
    public static void initialize() {
        mockStatic(ClientBuilder.class);
        client = mock(Client.class);
        when(ClientBuilder.newClient()).thenReturn(client);
        tilgangService = spy(new TilgangService());
    }

    @Before
    public void setUp() {
        // Mock REST-klienten
        Invocation.Builder builderMock = mock(Invocation.Builder.class);
        when(builderMock.get()).thenReturn(tilgangskontrollResponse);
        when(builderMock.header(anyString(), anyString())).thenReturn(builderMock);

        final WebTarget webTargetMock = mock(WebTarget.class);
        when(webTargetMock.request(APPLICATION_JSON)).thenReturn(builderMock);
        when(webTargetMock.queryParam(anyString(), anyString())).thenReturn(webTargetMock);

        when(client.target(anyString())).thenReturn(webTargetMock);

        gittBrukerMedOidcAssertation();
    }

    private void gittBrukerMedOidcAssertation() {
        Subject subject = new Subject(FNR, IdentType.InternBruker, SSO_TOKEN);
        subjectRule.setSubject(subject);
    }
}

package no.nav.syfo.rest.services;

import no.nav.brukerdialog.security.context.SubjectHandler;
import org.springframework.cache.annotation.Cacheable;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static java.lang.System.getProperty;
import static javax.ws.rs.client.ClientBuilder.newClient;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

public class TilgangService {

    private Client client = newClient();

    private static final String TILGANGSKONTROLL_API_NOKKEL = "TILGANGSKONTROLLAPI_URL";
    private static final String TILGANG_TIL_BRUKER_PATH = "/tilgangtilbruker";
    private static final String TILGANG_TIL_ENHETPATH = "/tilgangtilenhet";

    @Cacheable(value = "tilgang", keyGenerator = "userkeygenerator")
    public Response sjekkTilgangTilPerson(String fnr) {
        String ssoToken = SubjectHandler.getSubjectHandler().getInternSsoToken();
        Response response = client.target(hentTilgangskontrollUrl(TILGANG_TIL_BRUKER_PATH))
                .queryParam("fnr", fnr)
                .request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + ssoToken)
                .get();

        final int status = response.getStatus();
        if (200 != status && 403 != status) {
            throw new WebApplicationException(response);
        }
        return response;
    }

    @Cacheable(value = "tilgang", keyGenerator = "userkeygenerator")
    public Response sjekkTilgangTilEnhet(String enhet) {
        String ssoToken = SubjectHandler.getSubjectHandler().getInternSsoToken();
        Response response = client.target(hentTilgangskontrollUrl(TILGANG_TIL_ENHETPATH))
                .queryParam("enhet", enhet)
                .request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + ssoToken)
                .get();

        final int status = response.getStatus();
        if (200 != status && 403 != status) {
            throw new WebApplicationException(response);
        }
        return response;
    }

    private String hentTilgangskontrollUrl(String url) {
        return getProperty(TILGANGSKONTROLL_API_NOKKEL) + url;
    }
}

package no.nav.syfo.service;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.domain.Fnr;
import no.nav.syfo.oidc.OIDCIssuer;
import no.nav.syfo.util.OIDCUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.ws.rs.ForbiddenException;
import java.net.URI;
import java.util.Collections;

import static java.util.Collections.singletonMap;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Service
public class TilgangService {

    public static final String FNR = "fnr";
    public static final String ENHET = "enhet";
    public static final String TILGANG_TIL_BRUKER_PATH = "/tilgangtilbruker";
    public static final String TILGANG_TIL_BRUKER_VIA_AZURE_PATH = "/bruker";
    public static final String TILGANG_TIL_ENHET_PATH = "/enhet";
    private static final String FNR_PLACEHOLDER = "{" + FNR + "}";
    private static final String ENHET_PLACEHOLDER = "{" + ENHET + "}";
    private final RestTemplate template;
    private final UriComponentsBuilder tilgangTilBrukerUriTemplate;
    private final UriComponentsBuilder tilgangTilBrukerViaAzureUriTemplate;
    private final UriComponentsBuilder tilgangTilEnhetUriTemplate;
    private OIDCRequestContextHolder contextHolder;

    public TilgangService(
            @Value("${tilgangskontrollapi.url}") String tilgangskontrollUrl,
            RestTemplate template,
            final OIDCRequestContextHolder contextHolder
    ) {
        tilgangTilBrukerUriTemplate = fromHttpUrl(tilgangskontrollUrl)
                .path(TILGANG_TIL_BRUKER_PATH)
                .queryParam(FNR, FNR_PLACEHOLDER);
        tilgangTilBrukerViaAzureUriTemplate = fromHttpUrl(tilgangskontrollUrl)
                .path(TILGANG_TIL_BRUKER_VIA_AZURE_PATH)
                .queryParam(FNR, FNR_PLACEHOLDER);
        tilgangTilEnhetUriTemplate = fromHttpUrl(tilgangskontrollUrl)
                .path(TILGANG_TIL_ENHET_PATH)
                .queryParam(ENHET, ENHET_PLACEHOLDER);
        this.template = template;
        this.contextHolder = contextHolder;
    }

    public void throwExceptionIfVeilederWithoutAccess(Fnr fnr) {
        boolean harTilgang = harVeilederTilgangTilPersonViaAzure(fnr.getFnr());
        if (!harTilgang) {
            throw new ForbiddenException();
        }
    }

    public boolean harVeilederTilgangTilPerson(String fnr) {
        URI tilgangTilBrukerUriMedFnr = tilgangTilBrukerUriTemplate.build(singletonMap(FNR, fnr));
        return kallUriMedTemplate(tilgangTilBrukerUriMedFnr);
    }

    public boolean harVeilederTilgangTilPersonViaAzure(String fnr) {
        try {
            URI uri = tilgangTilBrukerViaAzureUriTemplate.build(singletonMap(FNR, fnr));
            template.exchange(
                    uri,
                    HttpMethod.GET,
                    lagRequest(OIDCIssuer.AZURE),
                    String.class
            );
            return true;
        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == 403) {
                return false;
            } else {
                throw e;
            }
        }
    }

    public boolean harVeilederTilgangTilEnhet(String enhet) {
        URI tilgangTilEnhetUriMedFnr = tilgangTilEnhetUriTemplate.build(singletonMap(ENHET, enhet));
        return kallUriMedTemplate(tilgangTilEnhetUriMedFnr);
    }

    private HttpEntity<String> lagRequest(String issuer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + OIDCUtil.tokenFraOIDC(contextHolder, issuer));
        return new HttpEntity<>(headers);
    }

    private boolean kallUriMedTemplate(URI uri) {
        try {
            template.getForObject(uri, Object.class);
            return true;
        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == 403) {
                return false;
            } else {
                throw e;
            }
        }
    }
}

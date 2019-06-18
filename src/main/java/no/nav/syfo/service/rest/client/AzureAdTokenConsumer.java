package no.nav.syfo.service.rest.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@Component
public class AzureAdTokenConsumer {
    private RestTemplate restTemplateMedProxy;
    private String url;
    private String clientId;
    private String clientSecret;

    public AzureAdTokenConsumer(RestTemplate restTemplateMedProxy,
                                @Value("${aadaccesstoken.url}") String url,
                                @Value("${aad_syfomoteadmin_clientid.username}") String clientId,
                                @Value("${aad_syfomoteadmin_clientid.password}") String clientSecret) {
        this.restTemplateMedProxy = restTemplateMedProxy;
        this.url = url;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    String getAccessToken(String resource) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        final MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("resource", resource);
        body.add("grant_type", "client_credentials");
        body.add("client_secret", clientSecret);
        final String uriString = UriComponentsBuilder.fromHttpUrl(url).toUriString();
        final ResponseEntity<AzureAdToken> result = restTemplateMedProxy.exchange(uriString, POST, new HttpEntity<>(body, headers), AzureAdToken.class);
        if (result.getStatusCode() != OK) {
            throw new RuntimeException("Henting av token fra Azure AD feiler med HTTP-" + result.getStatusCode());
        }
        return requireNonNull(result.getBody()).getAccess_token();
    }
}

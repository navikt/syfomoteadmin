package no.nav.syfo.service.rest.client;

import lombok.*;

@Value
@Builder
@Getter
class AzureAdToken {
    private String access_token;
    private String token_type;
    private String expires_in;
    private String ext_expires_in;
    private String expires_on;
    private String not_before;
    private String resource;
}

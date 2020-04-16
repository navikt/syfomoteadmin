package no.nav.syfo.service.ldap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class LdapNoAttributesFoundException extends WebApplicationException {
    public LdapNoAttributesFoundException(String message) {
        super(message, null, Response.Status.INTERNAL_SERVER_ERROR);
    }
}

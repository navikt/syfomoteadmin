package no.nav.syfo.rest.api.feil;

import org.slf4j.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.syfo.rest.api.feil.Feilmelding.Feil.GENERELL_FEIL;
import static no.nav.syfo.rest.api.feil.Feilmelding.NO_BIGIP_5XX_REDIRECT;
import static org.slf4j.LoggerFactory.getLogger;

@Provider
public class ThrowableMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = getLogger(ThrowableMapper.class);

    @Override
    public Response toResponse(Throwable e) {
        LOG.error("Uventet feil", e);

        Feilmelding melding = new Feilmelding().withFeil(GENERELL_FEIL);

        return Response.status(statuskode(e))
                .type(APPLICATION_JSON)
                .entity(melding)
                .header(NO_BIGIP_5XX_REDIRECT, true)
                .build();
    }

    private int statuskode(Throwable e) {
        if (e instanceof WebApplicationException) {
            return ((WebApplicationException) e).getResponse().getStatus();
        } else {
            return GENERELL_FEIL.status.getStatusCode();
        }
    }
}

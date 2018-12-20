package no.nav.syfo.api.ressurser;

import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.ok;

@Controller
@Path("/internal")
public class InternalResource {

    @GET
    @Path("/ping")
    public Response ping() {
        return ok("ping").build();
    }
}

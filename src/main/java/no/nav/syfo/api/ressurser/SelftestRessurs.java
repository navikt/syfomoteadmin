package no.nav.syfo.api.ressurser;

import no.nav.security.oidc.api.Unprotected;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Unprotected
@RequestMapping(value = "/internal")
public class SelftestRessurs {
    private static final String APPLICATION_LIVENESS = "Application is alive!";
    private static final String APPLICATION_READY = "Application is ready!";

    @Unprotected
    @ResponseBody
    @RequestMapping(value = "/isAlive", produces = MediaType.TEXT_PLAIN_VALUE)
    public String isAlive() {
        return APPLICATION_LIVENESS;
    }

    @Unprotected
    @ResponseBody
    @RequestMapping(value = "/isReady", produces = MediaType.TEXT_PLAIN_VALUE)
    public String isReady() {
        return APPLICATION_READY;
    }
}

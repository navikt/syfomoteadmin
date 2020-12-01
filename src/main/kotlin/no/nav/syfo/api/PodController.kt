package no.nav.syfo.api

import no.nav.security.oidc.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Unprotected
@RestController
@RequestMapping(value = ["/internal"])
class PodController {
    @RequestMapping(value = ["/isAlive"], produces = [MediaType.TEXT_PLAIN_VALUE])
    fun isAlive(): String {
        return "Application is alive!"
    }

    @RequestMapping(value = ["/isReady"], produces = [MediaType.TEXT_PLAIN_VALUE])
    fun isReady(): String {
        return "Application is ready!"
    }
}

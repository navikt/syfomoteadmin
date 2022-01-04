package no.nav.syfo.cache

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = SimpleCacheManager()
        cacheManager.setCaches(Arrays.asList(
            ConcurrentMapCache(CACHENAME_AXSYS_ENHETER),
            ConcurrentMapCache(CACHENAME_BEHANDLENDEENHET_FNR),
            ConcurrentMapCache(CACHENAME_DKIF_IDENT),
            ConcurrentMapCache(CACHENAME_EREG_VIRKSOMHETSNAVN),
            ConcurrentMapCache(CACHENAME_LDAP_VEILEDER_NAVN),
            ConcurrentMapCache(CACHENAME_NARMESTELEDER_ANSATTE),
            ConcurrentMapCache(CACHENAME_NARMESTELEDER_LEDER),
            ConcurrentMapCache(CACHENAME_ISNARMESTELEDER_LEDERE),
            ConcurrentMapCache(CACHENAME_ISNARMESTELEDER_LEDERRELASJONER),
            ConcurrentMapCache(CACHENAME_NARMESTELEDER_LEDERE),
            ConcurrentMapCache(CACHENAME_NORG_ENHETER),
            ConcurrentMapCache(CACHENAME_TILGANG_IDENT)
        ))
        return cacheManager
    }

    companion object {
        const val CACHENAME_BEHANDLENDEENHET_FNR = "behandlendeenhetfnr"
        const val CACHENAME_DKIF_IDENT = "dkifident"
        const val CACHENAME_EREG_VIRKSOMHETSNAVN = "virksomhetsnavn"
        const val CACHENAME_LDAP_VEILEDER_NAVN = "ldapveiledernavn"
        const val CACHENAME_AXSYS_ENHETER = "axsysenheter"
        const val CACHENAME_NARMESTELEDER_ANSATTE = "ansatte"
        const val CACHENAME_NARMESTELEDER_LEDER = "leder"
        const val CACHENAME_ISNARMESTELEDER_LEDERE = "isnarmesteleder_ledere"
        const val CACHENAME_ISNARMESTELEDER_LEDERRELASJONER = "isnarmesteleder_lederrelasjoner"
        const val CACHENAME_NARMESTELEDER_LEDERE = "ledere"
        const val CACHENAME_NORG_ENHETER = "norgenheter"
        const val CACHENAME_TILGANG_IDENT = "tilgangtilident"
    }
}

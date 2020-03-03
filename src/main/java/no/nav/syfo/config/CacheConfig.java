package no.nav.syfo.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.Arrays.asList;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHENAME_AKTOR_ID = "aktoerid";
    public static final String CACHENAME_AKTOR_FNR = "aktoerfnr";
    public static final String CACHENAME_BEHANDLENDEENHET_FNR = "behandlendeenhetfnr";
    public static final String CACHENAME_DKIF_AKTORID = "dkifaktorid";
    public static final String CACHENAME_DKIF_FNR = "dkiffnr";
    public static final String CACHENAME_EREG_NAVN = "eregnavn";
    public static final String CACHENAME_LDAP_VEILEDER = "ldapveileder";
    public static final String CACHENAME_NORG_ENHETER = "norgenheter";
    public static final String CACHENAME_PDL_PERSON = "pdlperson";
    public static final String CACHENAME_TILGANG_IDENT = "tilgangtilident";
    public static final String CACHENAME_TPS_BRUKER = "tpsbruker";
    public static final String CACHENAME_TPS_NAVN = "tpsnavn";


    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(asList(
                new ConcurrentMapCache(CACHENAME_AKTOR_ID),
                new ConcurrentMapCache(CACHENAME_AKTOR_FNR),
                new ConcurrentMapCache(CACHENAME_BEHANDLENDEENHET_FNR),
                new ConcurrentMapCache(CACHENAME_DKIF_AKTORID),
                new ConcurrentMapCache(CACHENAME_DKIF_FNR),
                new ConcurrentMapCache(CACHENAME_EREG_NAVN),
                new ConcurrentMapCache(CACHENAME_LDAP_VEILEDER),
                new ConcurrentMapCache(CACHENAME_NORG_ENHETER),
                new ConcurrentMapCache(CACHENAME_PDL_PERSON),
                new ConcurrentMapCache(CACHENAME_TILGANG_IDENT),
                new ConcurrentMapCache(CACHENAME_TPS_BRUKER),
                new ConcurrentMapCache(CACHENAME_TPS_NAVN)
        ));
        return cacheManager;
    }
}

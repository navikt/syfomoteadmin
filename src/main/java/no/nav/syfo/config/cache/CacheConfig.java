package no.nav.syfo.config.cache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static net.sf.ehcache.config.PersistenceConfiguration.Strategy.NONE;
import static net.sf.ehcache.store.MemoryStoreEvictionPolicy.LRU;
import static no.nav.dialogarena.aktor.AktorConfig.AKTOR_ID_FROM_FNR_CACHE;
import static no.nav.dialogarena.aktor.AktorConfig.FNR_FROM_AKTOR_ID_CACHE;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    @Bean
    public CacheManager ehCacheManager() {
        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        config.addCache(FNR_FROM_AKTOR_ID_CACHE);
        config.addCache(AKTOR_ID_FROM_FNR_CACHE);
        config.addCache(setupCache("dkif"));
        config.addCache(setupCache("egenansatt"));
        config.addCache(setupCache("ereg"));
        config.addCache(setupCache("ldap"));
        config.addCache(setupCache("norg"));
        config.addCache(setupCache("syfo"));
        config.addCache(setupCache("tilgang"));
        config.addCache(setupCache("tpsnavn"));
        config.addCache(setupCache("tpsbruker"));
        config.addCache(setupCache("person"));
        return CacheManager.newInstance(config);
    }

    @Bean
    public UserKeyGenerator userkeygenerator() {
        return new UserKeyGenerator();
    }

    @Override
    public org.springframework.cache.CacheManager cacheManager() {
        return new EhCacheCacheManager(ehCacheManager());
    }

    @Override
    public CacheResolver cacheResolver() {
        return null;
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return null;
    }

    private static CacheConfiguration setupCache(String name) {
        return new CacheConfiguration(name, 1000)
                .memoryStoreEvictionPolicy(LRU)
                .timeToIdleSeconds(3600)
                .timeToLiveSeconds(3600)
                .persistence(new PersistenceConfiguration().strategy(NONE));
    }
}

package no.nav.syfo.service;

import no.nav.syfo.service.ldap.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

import static java.util.Arrays.asList;
import static no.nav.syfo.config.CacheConfig.CACHENAME_LDAP_VEILEDER_NAVN;

@Service
public class VeilederService {

    private LdapService ldapService;

    @Inject
    public VeilederService(LdapService ldapService) {
        this.ldapService = ldapService;
    }

    @Cacheable(value = CACHENAME_LDAP_VEILEDER_NAVN, key = "#veilederUid", condition = "#veilederUid != null")
    public String hentNavn(String veilederUid) {
        Map map = ldapService.hentVeilederAttributter(veilederUid, asList("givenname", "sn"));
        return map.get("givenname").toString() + " " + map.get("sn").toString();
    }

    public Optional<String> hentVeilederNavn(String veilederUid) {
        try {
            return Optional.of(hentNavn(veilederUid));
        } catch (LdapNoAttributesFoundException e) {
            return Optional.empty();
        }
    }
}

package no.nav.syfo.service;


import no.nav.syfo.domain.model.Veileder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Map;

import static java.util.Arrays.asList;
import static no.nav.syfo.config.CacheConfig.CACHENAME_LDAP_VEILEDER;

@Service
public class VeilederService {

    private LdapService ldapService;

    @Inject
    public VeilederService(LdapService ldapService) {
        this.ldapService = ldapService;
    }

    private String hentVeilederEpost(String veilederUid) {
        return ldapService.hentVeilederAttributter(veilederUid, asList("mail")).get("mail").toString();
    }

    private String hentVeilederNavn(String veilederUid) {
        Map map = ldapService.hentVeilederAttributter(veilederUid, asList("givenname", "sn"));
        return map.get("givenname").toString() + " " + map.get("sn").toString();
    }

    @Cacheable(value = CACHENAME_LDAP_VEILEDER, key = "#veilederUid", condition = "#veilederUid != null")
    public Veileder hentVeileder(String veilederUid) {
        return new Veileder()
                .userId(veilederUid)
                .navn(hentVeilederNavn(veilederUid))
                .epost(hentVeilederEpost(veilederUid));
    }
}

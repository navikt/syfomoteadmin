package no.nav.syfo.service;


import no.nav.syfo.domain.model.Veileder;
import org.springframework.cache.annotation.Cacheable;

import javax.inject.Inject;
import java.util.Map;

import static java.util.Arrays.asList;

public class VeilederService {

    @Inject
    private LdapService ldapService;

    private String hentVeilederEpost(String veilederUid) {
        return ldapService.hentVeilederAttributter(veilederUid, asList("mail")).get("mail").toString();
    }

    private String hentVeilederNavn(String veilederUid) {
        Map map = ldapService.hentVeilederAttributter(veilederUid, asList("givenname", "sn"));
        return map.get("givenname").toString() + " " + map.get("sn").toString();
    }

    @Cacheable(value = "ldap", keyGenerator = "userkeygenerator")
    public Veileder hentVeileder(String veilederUid) {
        return new Veileder()
                .userId(veilederUid)
                .navn(hentVeilederNavn(veilederUid))
                .epost(hentVeilederEpost(veilederUid));
    }
}
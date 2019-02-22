package no.nav.syfo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

//må bruke Hashtable i InitiallLdapContext dessverre.
@SuppressWarnings({"squid:S1149"})
@Service
public class LdapService {

    private static Hashtable<String, String> env = new Hashtable<>();
    @Value("${ldap.basedn}")
    private String ldapBasedn;

    private static String SEARCHBASE;

    @Autowired
    public LdapService(Environment springEnv) {
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, springEnv.getRequiredProperty("ldap.url"));
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, springEnv.getRequiredProperty("ldap.username"));
        env.put(Context.SECURITY_CREDENTIALS, springEnv.getRequiredProperty("ldap.password"));

        SEARCHBASE = "OU=Users,OU=NAV,OU=BusinessUnits," + springEnv.getRequiredProperty("ldap.basedn");
    }

    public Map hentVeilederAttributter(String veilederUid, List<String> attributter) {
        Map map = new HashMap<>();

        try {
            SearchControls searchCtrl = new SearchControls();
            searchCtrl.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration<SearchResult> result = ldapContext().search(SEARCHBASE, String.format("(&(objectClass=user)(CN=%s))", veilederUid), searchCtrl);
            Attributes ldapAttributes = result.next().getAttributes();
            populateAttributtMap(attributter, map, ldapAttributes);

        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    private void populateAttributtMap(List<String> attributter, Map map, Attributes ldapAttributes) {
        attributter.forEach(s -> {
            try {
                map.put(s, ofNullable(ldapAttributes.get(s).get()).orElseThrow(() -> new RuntimeException("Fant ikke attributt " + s)));
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static LdapContext ldapContext() throws NamingException {
        return new InitialLdapContext(env, null);
    }
}

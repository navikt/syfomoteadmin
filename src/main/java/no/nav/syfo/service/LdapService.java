package no.nav.syfo.service;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static java.lang.System.getProperty;
import static java.util.Optional.ofNullable;

//må bruke Hashtable i InitiallLdapContext dessverre.
@SuppressWarnings({"squid:S1149"})
public class LdapService {
    private static Hashtable<String, String> env = new Hashtable<>();

    static {
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, getProperty("LDAP_URL"));
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, getProperty("LDAP_USERNAME"));
        env.put(Context.SECURITY_CREDENTIALS, getProperty("LDAP_PASSWORD"));
    }

    public Map hentVeilederAttributter(String veilederUid, List<String> attributter) {
        Map map = new HashMap<>();

        try {
            String searchbase = "OU=Users,OU=NAV,OU=BusinessUnits," + getProperty("LDAP_BASEDN");
            SearchControls searchCtrl = new SearchControls();
            searchCtrl.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration<SearchResult> result = ldapContext().search(searchbase, String.format("(&(objectClass=user)(CN=%s))", veilederUid), searchCtrl);
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

    public void ping() {
        try {
            String searchbase = "OU=Users,OU=NAV,OU=BusinessUnits," + getProperty("LDAP_BASEDN");
            Attributes ldapAttributes = new BasicAttributes();
            ldapContext().search(searchbase, ldapAttributes);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    private static LdapContext ldapContext() throws NamingException {
        return new InitialLdapContext(env, null);
    }
}

package no.nav.syfo.service.ldap;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;
import java.util.*;

import static java.util.Optional.ofNullable;

//må bruke Hashtable i InitiallLdapContext dessverre.
@SuppressWarnings({"squid:S1149"})
@Service
public class LdapService {

    private static final Logger LOG = LoggerFactory.getLogger(LdapService.class);

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
            if (result.hasMore()) {
                Attributes ldapAttributes = result.next().getAttributes();
                populateAttributtMap(attributter, map, ldapAttributes);
            } else {
                String message = "Fant ingen attributter i resultat fra ldap for veileder " + veilederUid;
                LOG.warn(message);
                throw new LdapNoAttributesFoundException(message);
            }
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

package no.nav.syfo.testhelper;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import no.nav.security.oidc.context.*;
import no.nav.security.spring.oidc.test.JwtTokenGenerator;
import no.nav.syfo.oidc.OIDCIssuer;

import java.text.ParseException;

public class OidcTestHelper {

    public static void loggInnVeilederAzure(OIDCRequestContextHolder oidcRequestContextHolder, String veilederIdent) throws ParseException {
        JWTClaimsSet claimsSet = JWTClaimsSet.parse("{\"NAVident\":\"" + veilederIdent + "\"}");
        SignedJWT jwt = JwtTokenGenerator.createSignedJWT(claimsSet);
        settOIDCValidationContext(oidcRequestContextHolder, jwt, OIDCIssuer.AZURE);
    }

    public static void loggInnVeileder(OIDCRequestContextHolder oidcRequestContextHolder, String subject) {
        SignedJWT jwt = JwtTokenGenerator.createSignedJWT(subject);

        settOIDCValidationContext(oidcRequestContextHolder, jwt, OIDCIssuer.INTERN);
    }

    public static void loggInnBruker(OIDCRequestContextHolder oidcRequestContextHolder, String subject) {
        SignedJWT jwt = JwtTokenGenerator.createSignedJWT(subject);
        settOIDCValidationContext(oidcRequestContextHolder, jwt, OIDCIssuer.EKSTERN);
    }

    private static void settOIDCValidationContext(OIDCRequestContextHolder oidcRequestContextHolder, SignedJWT jwt, String issuer) {
        TokenContext tokenContext = new TokenContext(issuer, jwt.serialize());
        OIDCClaims oidcClaims = new OIDCClaims(jwt);
        OIDCValidationContext oidcValidationContext = new OIDCValidationContext();
        oidcValidationContext.addValidatedToken(issuer, tokenContext, oidcClaims);
        oidcRequestContextHolder.setOIDCValidationContext(oidcValidationContext);
    }

    public static OIDCValidationContext lagOIDCValidationContextIntern(String subject) {
        return lagOIDCValidationContext(subject, OIDCIssuer.INTERN);
    }

    public static OIDCValidationContext lagOIDCValidationContextEkstern(String subject) {
        return lagOIDCValidationContext(subject, OIDCIssuer.EKSTERN);
    }

    private static OIDCValidationContext lagOIDCValidationContext(String subject, String issuer) {
        SignedJWT jwt = JwtTokenGenerator.createSignedJWT(subject);
        TokenContext tokenContext = new TokenContext(issuer, jwt.serialize());
        OIDCClaims oidcClaims = new OIDCClaims(jwt);
        OIDCValidationContext oidcValidationContext = new OIDCValidationContext();
        oidcValidationContext.addValidatedToken(issuer, tokenContext, oidcClaims);
        return oidcValidationContext;
    }

    public static void loggUtAlle(OIDCRequestContextHolder oidcRequestContextHolder) {
        oidcRequestContextHolder.setOIDCValidationContext(null);
    }

}

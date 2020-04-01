package no.nav.syfo.util;

import com.nimbusds.jwt.JWTClaimsSet;
import no.nav.security.oidc.OIDCConstants;
import no.nav.security.oidc.context.*;
import no.nav.syfo.oidc.*;

import java.text.ParseException;
import java.util.Optional;

import static no.nav.security.oidc.OIDCConstants.OIDC_VALIDATION_CONTEXT;

public class OIDCUtil {

    private static OIDCValidationContext context(OIDCRequestContextHolder contextHolder) {
        return Optional.ofNullable(contextHolder.getOIDCValidationContext())
                .orElse(null);
    }

    private static OIDCClaims claims(OIDCRequestContextHolder contextHolder, String issuer) {
        return Optional.ofNullable(context(contextHolder))
                .map(s -> s.getClaims(issuer))
                .orElse(null);
    }

    private static JWTClaimsSet claimSet(OIDCRequestContextHolder contextHolder, String issuer) {
        return Optional.ofNullable(claims(contextHolder, issuer))
                .map(OIDCClaims::getClaimSet)
                .orElse(null);
    }

    public static String getSubjectEkstern(OIDCRequestContextHolder contextHolder) {
        return Optional.ofNullable(claimSet(contextHolder, OIDCIssuer.EKSTERN))
                .map(JWTClaimsSet::getSubject)
                .orElse(null);
    }

    public static String getIssuerToken(OIDCRequestContextHolder contextHolder, String issuer) {
        OIDCValidationContext context = (OIDCValidationContext) contextHolder
                .getRequestAttribute(OIDC_VALIDATION_CONTEXT);
        TokenContext tokenContext = context.getToken(issuer);
        return tokenContext.getIdToken();
    }

    public static String getSubjectInternAzure(OIDCRequestContextHolder contextHolder) {
        OIDCValidationContext context = (OIDCValidationContext) contextHolder
                .getRequestAttribute(OIDC_VALIDATION_CONTEXT);
        try {
            return context.getClaims(OIDCIssuer.AZURE).getClaimSet().getStringClaim(OIDCClaim.NAVIDENT);
        } catch (ParseException e) {
            throw new RuntimeException("Klarte ikke hente veileder-ident ut av OIDC-token (Azure)");
        }
    }

    public static String tokenFraOIDC(OIDCRequestContextHolder contextHolder, String issuer) {
        OIDCValidationContext context = (OIDCValidationContext) contextHolder
                .getRequestAttribute(OIDCConstants.OIDC_VALIDATION_CONTEXT);

        return context.getToken(issuer).getIdToken();
    }
}

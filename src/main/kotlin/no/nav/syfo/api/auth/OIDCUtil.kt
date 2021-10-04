package no.nav.syfo.api.auth

import com.nimbusds.jwt.JWTClaimsSet
import no.nav.security.oidc.OIDCConstants
import no.nav.security.oidc.context.*
import java.text.ParseException
import java.util.*

object OIDCUtil {
    private fun context(contextHolder: OIDCRequestContextHolder): OIDCValidationContext {
        return Optional.ofNullable(contextHolder.oidcValidationContext)
            .orElse(null)
    }

    private fun claims(contextHolder: OIDCRequestContextHolder, issuer: String): OIDCClaims {
        return Optional.ofNullable(context(contextHolder))
            .map { s: OIDCValidationContext -> s.getClaims(issuer) }
            .orElse(null)
    }

    private fun claimSet(contextHolder: OIDCRequestContextHolder, issuer: String): JWTClaimsSet {
        return Optional.ofNullable(claims(contextHolder, issuer))
            .map { obj: OIDCClaims -> obj.claimSet }
            .orElse(null)
    }

    @JvmStatic
    fun getSubjectEkstern(contextHolder: OIDCRequestContextHolder): String? {
        return Optional.ofNullable(claimSet(contextHolder, OIDCIssuer.EKSTERN))
            .map { obj: JWTClaimsSet -> obj.subject }
            .orElse(null)
    }

    @JvmStatic
    fun getSubjectInternAzureV2(contextHolder: OIDCRequestContextHolder): String {
        val context = contextHolder
            .getRequestAttribute(OIDCConstants.OIDC_VALIDATION_CONTEXT) as OIDCValidationContext
        return try {
            context.getClaims(OIDCIssuer.VEILEDER_AZURE_V2).claimSet.getStringClaim(OIDCClaim.NAVIDENT)
        } catch (e: ParseException) {
            throw RuntimeException("Klarte ikke hente veileder-ident ut av OIDC-token (Azure)")
        }
    }

    @JvmStatic
    fun getAzpAzureV2(contextHolder: OIDCRequestContextHolder): String {
        val context = contextHolder
            .getRequestAttribute(OIDCConstants.OIDC_VALIDATION_CONTEXT) as OIDCValidationContext
        return try {
            context.getClaims(OIDCIssuer.VEILEDER_AZURE_V2).claimSet.getStringClaim(OIDCClaim.JWT_CLAIM_AZP)
        } catch (e: ParseException) {
            throw RuntimeException("Klarte ikke hente veileder-ident ut av OIDC-token (Azure)")
        }
    }

    fun tokenFraOIDC(contextHolder: OIDCRequestContextHolder, issuer: String?): String {
        val context = contextHolder
            .getRequestAttribute(OIDCConstants.OIDC_VALIDATION_CONTEXT) as OIDCValidationContext
        return context.getToken(issuer).idToken
    }
}

package no.nav.syfo.testhelper

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.mockk.every
import no.nav.security.oidc.context.*
import no.nav.security.oidc.test.support.JwtTokenGenerator
import no.nav.syfo.api.auth.OIDCIssuer
import no.nav.syfo.api.auth.OIDCUtil
import java.text.ParseException

object OidcTestHelper {
    @JvmStatic
    @Throws(ParseException::class)
    fun loggInnVeilederAzureV2(
        oidcRequestContextHolder: OIDCRequestContextHolder,
        veilederIdent: String
    ) {
        val claimsSet = JWTClaimsSet.parse("{\"NAVident\":\"$veilederIdent\", \"azp\":\"clientid\"}")
        val jwt = JwtTokenGenerator.createSignedJWT(claimsSet)
        settOIDCValidationContext(oidcRequestContextHolder, jwt, OIDCIssuer.VEILEDER_AZURE_V2)
    }

    private fun settOIDCValidationContext(
        oidcRequestContextHolder: OIDCRequestContextHolder,
        jwt: SignedJWT,
        issuer: String
    ) {
        val tokenContext = TokenContext(issuer, jwt.serialize())
        val oidcClaims = OIDCClaims(jwt)
        val oidcValidationContext = OIDCValidationContext()
        oidcValidationContext.addValidatedToken(issuer, tokenContext, oidcClaims)
        oidcRequestContextHolder.oidcValidationContext = oidcValidationContext
    }

    @JvmStatic
    fun loggUtAlle(oidcRequestContextHolder: OIDCRequestContextHolder) {
        oidcRequestContextHolder.oidcValidationContext = null
    }

    fun mockOIDCUtils(contextHolder: OIDCRequestContextHolder) {
        every {
            OIDCUtil.tokenFraOIDC(contextHolder, OIDCIssuer.VEILEDER_AZURE_V2)
        } returns "token"

        every {
            OIDCUtil.getSubjectInternAzureV2(contextHolder)
        } returns UserConstants.VEILEDER_ID

        every {
            OIDCUtil.getAzpAzureV2(contextHolder)
        } returns "azp"
    }
}

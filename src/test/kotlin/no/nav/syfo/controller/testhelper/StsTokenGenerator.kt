package no.nav.syfo.controller.testhelper

import no.nav.syfo.sts.STSToken
import no.nav.syfo.testhelper.UserConstants

val stsToken = STSToken(
        access_token = UserConstants.STS_TOKEN,
        token_type = "Bearer",
        expires_in = 3600
)

fun generateStsToken(): STSToken {
    return stsToken
}

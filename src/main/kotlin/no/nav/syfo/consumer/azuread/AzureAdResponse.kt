package no.nav.syfo.consumer.azuread

import java.time.Instant

data class AzureAdResponse(
        val access_token: String,
        val token_type: String,
        val expires_in: String,
        val ext_expires_in: String,
        val expires_on: Instant,
        val not_before: String,
        val resource: String
)

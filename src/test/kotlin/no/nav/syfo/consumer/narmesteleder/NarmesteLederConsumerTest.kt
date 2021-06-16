package no.nav.syfo.consumer.narmesteleder

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import no.nav.syfo.consumer.azuread.AzureAdTokenConsumer
import no.nav.syfo.metric.Metric
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.time.LocalDate

internal class NarmesteLederConsumerTest {

    private val syfonarmestelederUrl = "https://syfonarmesteleder.nav.no"

    @MockK
    private lateinit var azureAdTokenConsumer: AzureAdTokenConsumer

    @MockK
    private lateinit var metric: Metric

    @MockK
    private lateinit var restTemplate: RestTemplate

    @Before
    fun setUp() = MockKAnnotations.init(this, relaxed = true)

    @Test
    fun `should return empty list when response body is null`() {
        val consumer = NarmesteLederConsumer(syfonarmestelederUrl, azureAdTokenConsumer, metric, restTemplate, "123")

        every {
            restTemplate.exchange(
                "$syfonarmestelederUrl/syfonarmesteleder/sykmeldt/123/narmesteledere",
                HttpMethod.GET,
                any(),
                object : ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {}
            )
        } returns ResponseEntity(null, null, HttpStatus.OK)

        val responseList: List<NarmesteLederRelasjon> = consumer.narmestelederRelasjonerLedere("123")

        Assertions.assertThat(responseList).isEmpty()
    }

    @Test
    fun `should return list of relasjon when response body is not null`() {
        val consumer = NarmesteLederConsumer(syfonarmestelederUrl, azureAdTokenConsumer, metric, restTemplate, "123")

        every {
            restTemplate.exchange(
                "$syfonarmestelederUrl/syfonarmesteleder/sykmeldt/123/narmesteledere",
                HttpMethod.GET,
                any(),
                object : ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {}
            )
        } returns ResponseEntity(
            listOf(
                NarmesteLederRelasjon(
                    aktorId = "999",
                    orgnummer = "123",
                    narmesteLederAktorId = "234",
                    narmesteLederTelefonnummer = "99999999",
                    narmesteLederEpost = "99999999@999.no",
                    aktivFom = LocalDate.now().minusYears(1),
                    aktivTom = LocalDate.now(),
                    arbeidsgiverForskutterer = true,
                    skrivetilgang = false,
                    tilganger = listOf(Tilgang.MOTE),
                    navn = "Lyr"
                )
            ), null, HttpStatus.OK
        )

        val responseList: List<NarmesteLederRelasjon> = consumer.narmestelederRelasjonerLedere("123")

        Assertions.assertThat(responseList).isNotEmpty
    }
}

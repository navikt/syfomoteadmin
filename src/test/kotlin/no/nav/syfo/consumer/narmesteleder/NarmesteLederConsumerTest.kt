package no.nav.syfo.consumer.narmesteleder

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.consumer.azuread.AzureAdTokenConsumer
import no.nav.syfo.consumer.azuread.v2.AzureAdV2TokenConsumer
import no.nav.syfo.metric.Metric
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.OidcTestHelper.mockOIDCUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.web.client.RestTemplate
import java.time.LocalDate

internal class NarmesteLederConsumerTest {

    private val syfonarmestelederUrl = "https://syfonarmesteleder.nav.no"
    private val isnarmestelederHost = "https://isnarmesteleder.nav.no"

    @MockK
    private lateinit var azureAdTokenConsumer: AzureAdTokenConsumer

    @MockK
    private lateinit var azureAdV2TokenConsumer: AzureAdV2TokenConsumer

    @MockK
    private lateinit var metric: Metric

    @MockK
    private lateinit var restTemplate: RestTemplate

    @MockK
    private lateinit var restTemplateWithProxy: RestTemplate

    @MockK
    private lateinit var contextHolder: OIDCRequestContextHolder

    private lateinit var consumer: NarmesteLederConsumer

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        consumer = NarmesteLederConsumer(
            isnarmestelederHost,
            "123",
            syfonarmestelederUrl,
            "123",
            azureAdTokenConsumer,
            azureAdV2TokenConsumer,
            metric,
            restTemplate,
            restTemplateWithProxy,
            contextHolder
        )
    }

    @Test
    fun `should return empty list when response body is null`() {
        every {
            restTemplate.exchange(
                "$syfonarmestelederUrl/syfonarmesteleder/sykmeldt/123/narmesteledere",
                HttpMethod.GET,
                any(),
                object : ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {}
            )
        } returns ResponseEntity(null, null, HttpStatus.OK)

        val responseList: List<NarmesteLederRelasjon> = consumer.narmestelederRelasjonerLedere("123")

        assertThat(responseList).isEmpty()
    }

    @Test
    fun `should return list of relasjon when response body is not null`() {
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

        assertThat(responseList).isNotEmpty
    }

    @Test
    fun `return current leder for given virksomhetsnummer from isnarmesteleder`() {
        mockOIDCUtils(contextHolder)
        every {
            restTemplateWithProxy.exchange(
                "$isnarmestelederHost/api/v1/narmestelederrelasjon/personident",
                HttpMethod.GET,
                any(),
                object : ParameterizedTypeReference<List<NarmesteLederRelasjonDTO>>() {}
            )
        } returns ResponseEntity(lederListWithActiveLeder, null, HttpStatus.OK)

        val actualLeder: NarmesteLederRelasjonDTO? = consumer.narmesteLeder(innbyggerIdent, activeVirksomhetsnummer)

        assertThat(actualLeder).usingRecursiveComparison().isEqualTo(activeLeder)
    }

    @Test
    fun `return null when no active leder for given virksomhetsnummer`() {
        mockOIDCUtils(contextHolder)
        every {
            restTemplateWithProxy.exchange(
                "$isnarmestelederHost/api/v1/narmestelederrelasjon/personident",
                HttpMethod.GET,
                any(),
                object : ParameterizedTypeReference<List<NarmesteLederRelasjonDTO>>() {}
            )
        } returns ResponseEntity(lederListWithoutActiveLeder, null, HttpStatus.OK)

        val actualLeder: NarmesteLederRelasjonDTO? = consumer.narmesteLeder(innbyggerIdent, activeVirksomhetsnummer)

        assertThat(actualLeder).isNull()
    }

    @Test(expected = RuntimeException::class)
    fun `throw RuntimeException if response body from isnarmesteleder is null`() {
        mockOIDCUtils(contextHolder)
        every {
            restTemplateWithProxy.exchange(
                "$isnarmestelederHost/api/v1/narmestelederrelasjon/personident",
                HttpMethod.GET,
                any(),
                object : ParameterizedTypeReference<List<NarmesteLederRelasjonDTO>>() {}
            )
        } returns ResponseEntity(null, null, HttpStatus.OK)

        consumer.narmesteLeder(innbyggerIdent, activeVirksomhetsnummer)
    }

    @Test
    fun `return current leder for given virksomhetsnummer from isnarmesteleder with system token`() {
        mockOIDCUtils(contextHolder)
        every {
            restTemplateWithProxy.exchange(
                "$isnarmestelederHost/api/system/v1/narmestelederrelasjoner",
                HttpMethod.GET,
                any(),
                object : ParameterizedTypeReference<List<NarmesteLederRelasjonDTO>>() {}
            )
        } returns ResponseEntity(lederListWithActiveLeder, null, HttpStatus.OK)

        val actualLederList: List<NarmesteLederRelasjonDTO> = consumer.ledereForInnbyggerSystem(innbyggerIdent)

        assertThat(actualLederList).usingRecursiveComparison().isEqualTo(lederListWithActiveLeder)
    }

    @Test
    fun `return only leder for given virksomhetsnummer from isnarmesteleder with system token when innbygger has both lederrelasjon and ansattrelasjon`() {
        mockOIDCUtils(contextHolder)
        every {
            restTemplateWithProxy.exchange(
                "$isnarmestelederHost/api/system/v1/narmestelederrelasjoner",
                HttpMethod.GET,
                any(),
                object : ParameterizedTypeReference<List<NarmesteLederRelasjonDTO>>() {}
            )
        } returns ResponseEntity(listWithIdentAsBothLederAndAnsatt, null, HttpStatus.OK)

        val actualLederList: List<NarmesteLederRelasjonDTO> = consumer.ledereForInnbyggerSystem(innbyggerIdent)

        assertThat(actualLederList).usingRecursiveComparison().isEqualTo(lederListWithActiveLeder)
    }

    @Test
    fun `return empty list when no ledere for given inbyggerident with system token`() {
        mockOIDCUtils(contextHolder)
        every {
            restTemplateWithProxy.exchange(
                "$isnarmestelederHost/api/system/v1/narmestelederrelasjoner",
                HttpMethod.GET,
                any(),
                object : ParameterizedTypeReference<List<NarmesteLederRelasjonDTO>>() {}
            )
        } returns ResponseEntity(emptyList(), null, HttpStatus.OK)

        val actualLederList: List<NarmesteLederRelasjonDTO> = consumer.ledereForInnbyggerSystem(innbyggerIdent)

        assertThat(actualLederList).isEmpty()
    }

    @Test(expected = RuntimeException::class)
    fun `throw RuntimeException if response body from isnarmesteleder is null with system token`() {
        mockOIDCUtils(contextHolder)
        every {
            restTemplateWithProxy.exchange(
                "$isnarmestelederHost/api/system/v1/narmestelederrelasjoner",
                HttpMethod.GET,
                any(),
                object : ParameterizedTypeReference<List<NarmesteLederRelasjonDTO>>() {}
            )
        } returns ResponseEntity(null, null, HttpStatus.OK)

        consumer.ledereForInnbyggerSystem(innbyggerIdent)
    }
}

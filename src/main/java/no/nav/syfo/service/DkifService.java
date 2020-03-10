package no.nav.syfo.service;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.config.consumer.DkifConfig;
import no.nav.syfo.domain.model.Kontaktinfo;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.xml.ws.soap.SOAPFaultException;
import java.time.OffsetDateTime;

import static java.util.Optional.ofNullable;
import static no.nav.syfo.config.CacheConfig.CACHENAME_DKIF_AKTORID;
import static no.nav.syfo.config.CacheConfig.CACHENAME_DKIF_FNR;
import static no.nav.syfo.domain.model.Kontaktinfo.FeilAarsak.*;
import static no.nav.syfo.util.OIDCUtil.getIssuerToken;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class DkifService {

    private static final Logger log = LoggerFactory.getLogger(DkifService.class);

    private AktoerService aktoerService;
    private DkifConfig dkifConfig;
    private OIDCRequestContextHolder contextHolder;

    @Inject
    public DkifService(
            AktoerService aktoerService,
            DkifConfig dkifConfig,
            OIDCRequestContextHolder contextHolder
    ) {
        this.aktoerService = aktoerService;
        this.dkifConfig = dkifConfig;
        this.contextHolder = contextHolder;
    }

    @Retryable(
            value = {SOAPFaultException.class},
            backoff = @Backoff(delay = 200, maxDelay = 1000)
    )
    @Cacheable(value = CACHENAME_DKIF_FNR, key = "#fnr", condition = "#fnr != null")
    public Kontaktinfo hentKontaktinfoFnr(String fnr, String oidcIssuer) {
        if (isBlank(fnr) || !fnr.matches("\\d{11}$")) {
            log.error("Forsøker å hente kontaktinfor for fnr {}", fnr);
            throw new RuntimeException();
        }

        try {
            String oidcToken = getIssuerToken(this.contextHolder, oidcIssuer);
            WSHentDigitalKontaktinformasjonRequest request = new WSHentDigitalKontaktinformasjonRequest().withPersonident(fnr);
            WSKontaktinformasjon response = dkifConfig.hentDigitalKontaktinformasjon(request, oidcToken).getDigitalKontaktinformasjon();

            if ("true".equalsIgnoreCase(response.getReservasjon())) {
                return new Kontaktinfo().skalHaVarsel(false).feilAarsak(RESERVERT);
            }

            if (!harVerfisertSiste18Mnd(response.getEpostadresse(), response.getMobiltelefonnummer())) {
                return new Kontaktinfo().skalHaVarsel(false).feilAarsak(UTGAATT);
            }

            return new Kontaktinfo()
                    .skalHaVarsel(true)
                    .epost(response.getEpostadresse() != null ? response.getEpostadresse().getValue() : "")
                    .tlf(response.getMobiltelefonnummer() != null ? response.getMobiltelefonnummer().getValue() : "");
        } catch (HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet e) {
            return new Kontaktinfo().skalHaVarsel(false).feilAarsak(KONTAKTINFO_IKKE_FUNNET);
        } catch (HentDigitalKontaktinformasjonSikkerhetsbegrensing e) {
            return new Kontaktinfo().skalHaVarsel(false).feilAarsak(SIKKERHETSBEGRENSNING);
        } catch (HentDigitalKontaktinformasjonPersonIkkeFunnet e) {
            return new Kontaktinfo().skalHaVarsel(false).feilAarsak(PERSON_IKKE_FUNNET);
        } catch (RuntimeException e) {
            log.error("Det skjedde en uventet feil mot DKIF. Kaster feil videre");
            throw e;
        }
    }

    @Recover
    public void recover(SOAPFaultException e) {
        log.error("Feil ved kall hentKontaktinfo for Ident etter maks antall kall", e);
        throw e;
    }

    public boolean harVerfisertSiste18Mnd(WSEpostadresse epostadresse, WSMobiltelefonnummer mobiltelefonnummer) {
        return harVerifisertEpostSiste18Mnd(epostadresse) && harVerifisertMobilSiste18Mnd(mobiltelefonnummer);
    }

    private boolean harVerifisertEpostSiste18Mnd(WSEpostadresse epostadresse) {
        return ofNullable(epostadresse)
                .map(WSEpostadresse::getSistVerifisert)
                .filter(sistVerifisertEpost -> sistVerifisertEpost.isAfter(OffsetDateTime.now().minusMonths(18)))
                .isPresent();
    }

    private boolean harVerifisertMobilSiste18Mnd(WSMobiltelefonnummer mobiltelefonnummer) {
        return ofNullable(mobiltelefonnummer)
                .map(WSMobiltelefonnummer::getSistVerifisert)
                .filter(sistVerifisertEpost -> sistVerifisertEpost.isAfter(OffsetDateTime.now().minusMonths(18)))
                .isPresent();
    }

    @Cacheable(value = CACHENAME_DKIF_AKTORID, key = "#aktoerId", condition = "#aktoerId != null")
    public Kontaktinfo hentKontaktinfoAktoerId(String aktoerId, String oidcIssuer) {
        return hentKontaktinfoFnr(aktoerService.hentFnrForAktoer(aktoerId), oidcIssuer);
    }
}

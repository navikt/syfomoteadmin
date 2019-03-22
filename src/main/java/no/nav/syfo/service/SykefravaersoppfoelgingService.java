package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.config.consumer.SykefravaersoppfoelgingConfig;
import no.nav.syfo.domain.model.Ansatt;
import no.nav.syfo.domain.model.NaermesteLeder;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.*;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.meldinger.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;

import javax.ws.rs.ForbiddenException;
import java.util.List;

import static java.util.Collections.emptyList;
import static no.nav.syfo.mappers.SykefravaersoppfoelgingMapper.ws2Ansatt;
import static no.nav.syfo.repository.mapper.NaermesteLederMapper.ws2naermesteleder;
import static no.nav.syfo.util.MapUtil.map;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.OIDCUtil.getIssuerToken;

@Slf4j
@Service
public class SykefravaersoppfoelgingService {

    @Value("${dev}")
    private String dev;

    private OIDCRequestContextHolder contextHolder;

    private SykefravaersoppfoelgingV1 sykefravaersoppfoelgingV1SystemBruker;

    private SykefravaersoppfoelgingConfig sykefravaersoppfoelgingConfig;

    @Autowired
    public SykefravaersoppfoelgingService(
            final OIDCRequestContextHolder contextHolder,
            @Qualifier("sykefravaersoppfoelgingV1SystemBruker") SykefravaersoppfoelgingV1 sykefravaersoppfoelgingV1SystemBruker,
            SykefravaersoppfoelgingConfig sykefravaersoppfoelgingConfig
    ) {
        this.contextHolder = contextHolder;
        this.sykefravaersoppfoelgingV1SystemBruker = sykefravaersoppfoelgingV1SystemBruker;
        this.sykefravaersoppfoelgingConfig = sykefravaersoppfoelgingConfig;
    }

    public List<Ansatt> hentNaermesteLedersAnsattListe(String nlAktoerId, String oidcIssuer) {
        try {
            WSHentNaermesteLedersAnsattListeRequest request = new WSHentNaermesteLedersAnsattListeRequest()
                    .withAktoerId(nlAktoerId);
            WSHentNaermesteLedersAnsattListeResponse response;

            String oidcToken = getIssuerToken(this.contextHolder, oidcIssuer);
            response = sykefravaersoppfoelgingConfig.hentNaermesteLedersAnsattListe(request, oidcToken);
            return mapListe(response.getAnsattListe(), ws2Ansatt);
        } catch (HentNaermesteLedersAnsattListeSikkerhetsbegrensning e) {
            log.error("Fikk ikke tilgang til å hente nærmeste leders ansatt-liste med nlAktoerId {}. Returnerer tom liste.", nlAktoerId, e);
            throw new ForbiddenException("Ikke tilgang til å hente nærmeste leders ansatt-liste", e);
        } catch (RuntimeException e) {
            log.error("Runtimefeil ved henting av nærmeste leders ansatt-liste for nlAktoerId {} av bruker. Returnerer tom liste.", nlAktoerId, e);
            return emptyList();
        }
    }

    public NaermesteLeder hentNaermesteLederSomBruker(String aktoerId, String orgnummer) {
        return hentNaermesteLeder(aktoerId, orgnummer, sykefravaersoppfoelgingV1SystemBruker);
    }

    public NaermesteLeder hentNaermesteLederSomSystembruker(String aktoerId, String orgnummer) {
        return hentNaermesteLeder(aktoerId, orgnummer, sykefravaersoppfoelgingV1SystemBruker);
    }

    private NaermesteLeder hentNaermesteLeder(String aktoerId, String orgnummer, SykefravaersoppfoelgingV1 sykefravaersoppfoelgingV1SystemBruker) {
        try {
            try {
                return map(sykefravaersoppfoelgingV1SystemBruker.hentNaermesteLeder(
                        new WSHentNaermesteLederRequest().withAktoerId(aktoerId).withOrgnummer(orgnummer)).getNaermesteLeder(), ws2naermesteleder);
            } catch (HentNaermesteLederSikkerhetsbegrensning e) {
                log.error("Bruker har ikke tilgang til å hente nærmeste leder for aktoerId {} og orgnummer {}", aktoerId, orgnummer, e);
                throw new ForbiddenException(e);
            }
        } catch (RuntimeException e) {
            log.error("Feil ved henting av nærmeste leder for bruker ved forespørsel om aktoerId {} og orgnummer {}. Returnerer tom liste.", aktoerId, orgnummer, e);
            throw new RuntimeException(e);
        }
    }
}

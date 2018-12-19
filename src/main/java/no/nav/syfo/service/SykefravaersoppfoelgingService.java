package no.nav.syfo.service;

import no.nav.syfo.domain.model.Ansatt;
import no.nav.syfo.domain.model.NaermesteLeder;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.HentNaermesteLederSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.HentNaermesteLedersAnsattListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.SykefravaersoppfoelgingV1;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.meldinger.WSHentNaermesteLederRequest;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.meldinger.WSHentNaermesteLedersAnsattListeRequest;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.util.List;

import static java.util.Collections.emptyList;
import static no.nav.syfo.mappers.SykefravaersoppfoelgingMapper.ws2Ansatt;
import static no.nav.syfo.repository.mapper.NaermesteLederMapper.ws2naermesteleder;
import static no.nav.syfo.util.MapUtil.map;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.SubjectHandlerUtil.getUserId;
import static org.slf4j.LoggerFactory.getLogger;

public class SykefravaersoppfoelgingService {
    private static final Logger LOG = getLogger(SykefravaersoppfoelgingService.class);

    @Inject
    private SykefravaersoppfoelgingV1 sykefravaersoppfoelgingV1;

    @Cacheable(value = "syfo", keyGenerator = "userkeygenerator")
    public List<Ansatt> hentNaermesteLedersAnsattListe(String nlAktoerId) {
        try {
            return mapListe(sykefravaersoppfoelgingV1.hentNaermesteLedersAnsattListe(
                    new WSHentNaermesteLedersAnsattListeRequest().withAktoerId(nlAktoerId)).getAnsattListe(), ws2Ansatt);
        } catch (HentNaermesteLedersAnsattListeSikkerhetsbegrensning e) {
            LOG.error("{} fikk Ikke tilgang til å hente nærmeste leders ansatt-liste med nlAktoerId {}. Returnerer tom liste.", getUserId(), nlAktoerId, e);
            throw new ForbiddenException("Ikke tilgang til å hente nærmeste leders ansatt-liste", e);
        } catch (RuntimeException e) {
            LOG.error("Runtimefeil ved henting av nærmeste leders ansatt-liste for nlAktoerId {} av bruker {}. Returnerer tom liste.", nlAktoerId, getUserId(), e);
            return emptyList();
        }
    }

    @Cacheable(value = "syfo", keyGenerator = "userkeygenerator")
    public NaermesteLeder hentNaermesteLeder(String aktoerId, String orgnummer) {
        try {
            try {
                return map(sykefravaersoppfoelgingV1.hentNaermesteLeder(
                        new WSHentNaermesteLederRequest().withAktoerId(aktoerId).withOrgnummer(orgnummer)).getNaermesteLeder(), ws2naermesteleder);
            } catch (HentNaermesteLederSikkerhetsbegrensning e) {
                LOG.error("Bruker {} har ikke tilgang til å hente nærmeste leder for aktoerId {} og orgnummer {}", getUserId(), aktoerId, orgnummer, e);
                throw new ForbiddenException(e);
            }
        } catch (RuntimeException e) {
            LOG.error("Feil ved henting av nærmeste leder for bruker {} ved forespørsel om aktoerId {} og orgnummer {}. Returnerer tom liste.", getUserId(), aktoerId, orgnummer, e);
            throw new RuntimeException(e);
        }
    }
}

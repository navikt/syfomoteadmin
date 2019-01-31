package no.nav.syfo.service;

import no.nav.tjeneste.virksomhet.person.v3.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.WSHentPersonRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

import javax.ws.rs.ForbiddenException;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

public class PersonService {
    private static final Logger LOG = getLogger(PersonService.class);
    private static final String KODE6 = "SPSF";
    private static final String KODE7 = "SPFO";

    @Autowired
    private PersonV3 personV3;

    public boolean erPersonKode6(String fnr) {
        return KODE6.equals(hentDiskresjonskodeForFnr(fnr));
    }

    private String hentDiskresjonskodeForFnr(String aktorId) {
        WSPerson person = hentPersonFraFnr(aktorId);
        return ofNullable(person.getDiskresjonskode())
                .map(WSDiskresjonskoder::getValue)
                .orElse("");

    }

    @Cacheable(value = "person", keyGenerator = "userkeygenerator")
    public WSPerson hentPersonFraFnr(String fnr) {
        if (isBlank(fnr) || !fnr.matches("\\d{11}$")) {
            LOG.error("Ugyldig format på fnr");
            throw new IllegalArgumentException();
        }
        try {
            WSNorskIdent norskIdent = new WSNorskIdent();
            norskIdent.setIdent(fnr);
            WSPersonIdent personIdent = new WSPersonIdent();
            personIdent.setIdent(norskIdent);

            WSHentPersonRequest request = new WSHentPersonRequest();
            request.setAktoer(personIdent);
            return personV3
                    .hentPerson(request)
                    .getPerson();
        } catch (HentPersonSikkerhetsbegrensning e) {
            LOG.error("Fikk sikkerhetsbegrensing ved oppslag med fnr");
            throw new ForbiddenException();
        } catch (HentPersonPersonIkkeFunnet e) {
            LOG.error("Fant ikke person");
            throw new RuntimeException();
        } catch (RuntimeException e) {
            LOG.error("Fikk RuntimeException mot TPS for person ved oppslag av person");
            throw new RuntimeException();
        }
    }
}

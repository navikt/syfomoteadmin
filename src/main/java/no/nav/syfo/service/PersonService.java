package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.person.v3.binding.*;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import static java.util.Optional.ofNullable;
import static no.nav.syfo.config.CacheConfig.CACHENAME_PERSON_PERSON;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Service
public class PersonService {
    private static final String KODE6 = "SPSF";
    private static final String KODE7 = "SPFO";

    private PersonV3 personV3;

    @Inject
    public PersonService(PersonV3 personV3) {
        this.personV3 = personV3;
    }

    public boolean erPersonKode6(String fnr) {
        return KODE6.equals(hentDiskresjonskodeForFnr(fnr));
    }

    private String hentDiskresjonskodeForFnr(String aktorId) {
        Person person = hentPersonFraFnr(aktorId);
        return ofNullable(person.getDiskresjonskode())
                .map(Diskresjonskoder::getValue)
                .orElse("");

    }

    @Cacheable(value = CACHENAME_PERSON_PERSON, key = "#fnr", condition = "#fnr != null")
    public Person hentPersonFraFnr(String fnr) {
        if (isBlank(fnr) || !fnr.matches("\\d{11}$")) {
            log.error("Ugyldig format på fnr");
            throw new IllegalArgumentException();
        }
        try {
            NorskIdent norskIdent = new NorskIdent();
            norskIdent.setIdent(fnr);
            PersonIdent personIdent = new PersonIdent();
            personIdent.setIdent(norskIdent);

            HentPersonRequest request = new HentPersonRequest();
            request.setAktoer(personIdent);
            return personV3
                    .hentPerson(request)
                    .getPerson();
        } catch (HentPersonSikkerhetsbegrensning e) {
            log.error("Fikk sikkerhetsbegrensing ved oppslag med fnr");
            throw new ForbiddenException();
        } catch (HentPersonPersonIkkeFunnet e) {
            log.error("Fant ikke person");
            throw new RuntimeException();
        } catch (RuntimeException e) {
            log.error("Fikk RuntimeException mot TPS for person ved oppslag av person");
            throw new RuntimeException();
        }
    }
}

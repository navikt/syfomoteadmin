package no.nav.syfo.service;

import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.AktoerId;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
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

    public boolean erPersonKode6(String aktorId) {
        return KODE6.equals(hentDiskresjonskodeForAktor(aktorId));
    }

    private String hentDiskresjonskodeForAktor(String aktorId) {
        Person person = hentPersonFraAktoerId(aktorId);
        return ofNullable(person.getDiskresjonskode())
                .map(Diskresjonskoder::getValue)
                .orElse("");

    }

    @Cacheable(value = "person", keyGenerator = "userkeygenerator")
    public Person hentPersonFraAktoerId(String aktorId) {
        if (isBlank(aktorId) || !aktorId.matches("\\d{13}$")) {
            LOG.error("Ugyldig format på aktoerId: " + aktorId);
            throw new IllegalArgumentException();
        }
        try {
            return personV3.hentPerson(new HentPersonRequest()
                    .withAktoer(new AktoerId()
                            .withAktoerId(aktorId)))
                    .getPerson();
        } catch (HentPersonSikkerhetsbegrensning e) {
            LOG.error("Fikk sikkerhetsbegrensing ved oppslag med aktorId: " + aktorId);
            throw new ForbiddenException();
        } catch (HentPersonPersonIkkeFunnet e) {
            LOG.error("Fant ikke person med aktorId: " + aktorId);
            throw new RuntimeException();
        } catch (RuntimeException e) {
            LOG.error("Fikk RuntimeException mot TPS for person ved oppslag av aktorId: " + aktorId);
            throw new RuntimeException();
        }
    }
}

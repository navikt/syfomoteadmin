package no.nav.syfo.config.mocks;

import no.nav.tjeneste.virksomhet.person.v3.binding.*;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static no.nav.syfo.config.consumer.PersonConfig.MOCK_KEY;

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
public class PersonV3Mock implements PersonV3 {
    @Override
    public HentPersonResponse hentPerson(HentPersonRequest wsHentPersonRequest) throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        return new HentPersonResponse()
                .withPerson(new Person()
                        .withAktoer(new PersonIdent()
                                .withIdent(new NorskIdent()
                                        .withIdent("1234567890123")))
                        .withDiskresjonskode(new Diskresjonskoder().withValue("SPSF"))
                );
    }

    @Override
    public HentGeografiskTilknytningResponse hentGeografiskTilknytning(HentGeografiskTilknytningRequest wsHentGeografiskTilknytningRequest) throws HentGeografiskTilknytningSikkerhetsbegrensing, HentGeografiskTilknytningPersonIkkeFunnet {
        return null;
    }

    @Override
    public HentVergeResponse hentVerge(HentVergeRequest request) throws HentVergePersonIkkeFunnet, HentVergeSikkerhetsbegrensning {
        return null;
    }

    @Override
    public HentEkteskapshistorikkResponse hentEkteskapshistorikk(HentEkteskapshistorikkRequest request) throws HentEkteskapshistorikkPersonIkkeFunnet, HentEkteskapshistorikkSikkerhetsbegrensning {
        return null;
    }

    @Override
    public HentPersonerMedSammeAdresseResponse hentPersonerMedSammeAdresse(HentPersonerMedSammeAdresseRequest request) throws HentPersonerMedSammeAdresseIkkeFunnet, HentPersonerMedSammeAdresseSikkerhetsbegrensning {
        return null;
    }

    @Override
    public HentPersonhistorikkResponse hentPersonhistorikk(HentPersonhistorikkRequest request) throws HentPersonhistorikkPersonIkkeFunnet, HentPersonhistorikkSikkerhetsbegrensning {
        return null;
    }

    @Override
    public HentSikkerhetstiltakResponse hentSikkerhetstiltak(HentSikkerhetstiltakRequest wsHentSikkerhetstiltakRequest) throws HentSikkerhetstiltakPersonIkkeFunnet {
        return null;
    }

    @Override
    public void ping() {

    }

    @Override
    public HentPersonnavnBolkResponse hentPersonnavnBolk(HentPersonnavnBolkRequest wsHentPersonnavnBolkRequest) {
        return null;
    }
}

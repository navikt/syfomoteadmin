package no.nav.syfo.config.mocks;

import no.nav.tjeneste.virksomhet.person.v3.*;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSDiskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSNorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSPerson;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSPersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static no.nav.syfo.config.consumer.PersonConfig.MOCK_KEY;

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
public class PersonV3Mock implements PersonV3 {
    @Override
    public WSHentPersonResponse hentPerson(WSHentPersonRequest wsHentPersonRequest) throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        return new WSHentPersonResponse()
                .withPerson(new WSPerson()
                        .withAktoer(new WSPersonIdent()
                                .withIdent(new WSNorskIdent()
                                        .withIdent("1234567890123")))
                        .withDiskresjonskode(new WSDiskresjonskoder().withValue("SPSF"))
                );
    }

    @Override
    public WSHentGeografiskTilknytningResponse hentGeografiskTilknytning(WSHentGeografiskTilknytningRequest wsHentGeografiskTilknytningRequest) throws HentGeografiskTilknytningSikkerhetsbegrensing, HentGeografiskTilknytningPersonIkkeFunnet {
        return null;
    }

    @Override
    public WSHentSikkerhetstiltakResponse hentSikkerhetstiltak(WSHentSikkerhetstiltakRequest wsHentSikkerhetstiltakRequest) throws HentSikkerhetstiltakPersonIkkeFunnet {
        return null;
    }

    @Override
    public void ping() {

    }

    @Override
    public WSHentPersonnavnBolkResponse hentPersonnavnBolk(WSHentPersonnavnBolkRequest wsHentPersonnavnBolkRequest) {
        return null;
    }
}

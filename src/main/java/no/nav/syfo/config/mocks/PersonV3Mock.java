package no.nav.syfo.config.mocks;

import no.nav.tjeneste.virksomhet.person.v3.binding.*;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.AktoerId;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.*;

public class PersonV3Mock implements PersonV3 {
    @Override
    public HentPersonResponse hentPerson(HentPersonRequest hentPersonRequest) throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        return new HentPersonResponse()
                .withPerson(new Person()
                        .withAktoer(new AktoerId().withAktoerId("1234567890123"))
                        .withDiskresjonskode(new Diskresjonskoder().withValue("SPSF"))
                );
    }

    @Override
    public void ping() {

    }

    @Override
    public HentPersonnavnBolkResponse hentPersonnavnBolk(HentPersonnavnBolkRequest hentPersonnavnBolkRequest) {
        return null;
    }

    @Override
    public HentSikkerhetstiltakResponse hentSikkerhetstiltak(HentSikkerhetstiltakRequest hentSikkerhetstiltakRequest) throws HentSikkerhetstiltakPersonIkkeFunnet {
        return null;
    }

    @Override
    public HentGeografiskTilknytningResponse hentGeografiskTilknytning(HentGeografiskTilknytningRequest hentGeografiskTilknytningRequest) throws HentGeografiskTilknytningPersonIkkeFunnet, HentGeografiskTilknytningSikkerhetsbegrensing {
        return null;
    }

    @Override
    public HentVergeResponse hentVerge(HentVergeRequest hentVergeRequest) throws HentVergePersonIkkeFunnet, HentVergeSikkerhetsbegrensning {
        return null;
    }

    @Override
    public HentEkteskapshistorikkResponse hentEkteskapshistorikk(HentEkteskapshistorikkRequest hentEkteskapshistorikkRequest) throws HentEkteskapshistorikkPersonIkkeFunnet, HentEkteskapshistorikkSikkerhetsbegrensning {
        return null;
    }

    @Override
    public HentPersonerMedSammeAdresseResponse hentPersonerMedSammeAdresse(HentPersonerMedSammeAdresseRequest hentPersonerMedSammeAdresseRequest) throws HentPersonerMedSammeAdresseIkkeFunnet, HentPersonerMedSammeAdresseSikkerhetsbegrensning {
        return null;
    }

    @Override
    public HentPersonhistorikkResponse hentPersonhistorikk(HentPersonhistorikkRequest hentPersonhistorikkRequest) throws HentPersonhistorikkPersonIkkeFunnet, HentPersonhistorikkSikkerhetsbegrensning {
        return null;
    }
}

package no.nav.syfo.config.mocks;

import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest;
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static no.nav.syfo.config.consumer.EgenAnsattConfig.MOCK_KEY;

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
public class EgenAnsattMock implements EgenAnsattV1 {
    @Override
    public void ping() {

    }

    @Override
    public WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse hentErEgenAnsattEllerIFamilieMedEgenAnsatt(WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest wsHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest) {
        return new WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse()
                .withEgenAnsatt(false);
    }
}

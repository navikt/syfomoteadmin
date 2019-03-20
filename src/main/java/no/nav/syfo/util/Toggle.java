package no.nav.syfo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Toggle {

    private boolean toggleSendeEpost;
    private boolean toggleBatchPaaminelse;
    private boolean toggleBatchEpost;

    public Toggle(
            @Value("${toggles.send.epost:true}") boolean toggleSendeEpost,
            @Value("${toggle.enable.batch.paaminnelse:false}") boolean toggleBatchPaaminelse,
            @Value("${toggle.enable.batch.epost:false}") boolean toggleBatchEpost
    ) {
        this.toggleSendeEpost = toggleSendeEpost;
        this.toggleBatchPaaminelse = toggleBatchPaaminelse;
        this.toggleBatchEpost = toggleBatchEpost;
    }

    public boolean toggleSendeEpost() {
        return toggleSendeEpost;
    }

    public boolean toggleBatchPaaminelse() {
        return toggleBatchPaaminelse;
    }

    public boolean toggleBatchEpost() {
        return toggleSendeEpost;
    }
}

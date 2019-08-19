package no.nav.syfo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Toggle {

    private boolean toggleBatchPaaminelse;

    public Toggle(
            @Value("${toggle.enable.batch.paaminnelse:true}") boolean toggleBatchPaaminelse
    ) {
        this.toggleBatchPaaminelse = toggleBatchPaaminelse;
    }

    public boolean toggleBatchPaaminelse() {
        return toggleBatchPaaminelse;
    }
}

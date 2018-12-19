package no.nav.syfo.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Accessors(fluent = true)
public class Oppgavevarsel {
    public Long id;
    public String type;
    public String ressursId;
    public String varselbestillingId;
    public String mottaker;
    public Map<String, String> parameterListe;
    public LocalDateTime utlopstidspunkt;
    public LocalDateTime utsendelsestidspunkt;
    public String varseltypeId;
    public String oppgavetype;
    public String oppgaveUrl;
    public boolean repeterendeVarsel;
}

package no.nav.syfo.repository.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class PMotedeltaker {
    public Long id;
    public String uuid;
    public String motedeltakertype;
    public long moteId;
    public String status;
    public LocalDateTime svarTidspunkt;
}

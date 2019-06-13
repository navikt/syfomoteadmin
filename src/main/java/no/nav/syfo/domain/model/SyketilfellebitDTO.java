package no.nav.syfo.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true, chain = true)
public class SyketilfellebitDTO {
    public String aktorId;
    @Nullable
    public String orgnummer;
    public LocalDateTime inntruffet;
    public String tags;
    public String ressursId;
    public LocalDateTime fom;
    public LocalDateTime tom;
}

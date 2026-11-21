package com.appointy.clinicalhistory.infrastructure.driven_adapters.sistema;

import com.appointy.clinicalhistory.domain.gateway.RelojGateway;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class RelojAdapter implements RelojGateway {

    private final Clock reloj;

    public RelojAdapter(Clock reloj) {
        this.reloj = reloj;
    }

    @Override
    public long ahora() {
        return reloj.millis();
    }

    @Override
    public String hoy() {
        return LocalDate.now(reloj).toString();
    }
}

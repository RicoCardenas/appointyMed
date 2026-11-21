package com.appointy.appointment.infrastructure.driven_adapters.sistema;

import com.appointy.appointment.domain.gateway.RelojGateway;
import com.appointy.appointment.domain.rule.FechaIso;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class RelojAdapter implements RelojGateway {

    private final Clock reloj;

    public RelojAdapter(Clock reloj) {
        this.reloj = reloj;
    }

    @Override
    public long ahoraMillis() {
        return reloj.millis();
    }

    @Override
    public String hoy() {
        return LocalDate.now(reloj).toString();
    }

    @Override
    public long ahoraMinutosLocales() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        return FechaIso.minutosLocales(ahora.toLocalDate().toString(), ahora.getHour() * 60 + ahora.getMinute());
    }
}

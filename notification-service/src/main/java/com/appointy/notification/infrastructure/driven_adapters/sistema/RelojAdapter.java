package com.appointy.notification.infrastructure.driven_adapters.sistema;

import com.appointy.notification.domain.exception.ValidacionException;
import com.appointy.notification.domain.gateway.RelojGateway;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class RelojAdapter implements RelojGateway {

    private final Clock reloj;
    private final ZoneId zona;

    public RelojAdapter(Clock reloj, ZoneId zona) {
        this.reloj = reloj;
        this.zona = zona;
    }

    @Override
    public long ahora() {
        return reloj.millis();
    }

    @Override
    public long inicioEnMillis(String fecha, String hora) {
        try {
            return LocalDate.parse(fecha).atTime(LocalTime.parse(hora)).atZone(zona).toInstant().toEpochMilli();
        } catch (DateTimeException e) {
            // El formato ya paso la regla del dominio, pero la fecha puede no existir (2026-13-45, 2026-02-30).
            throw new ValidacionException("FECHA_INVALIDA", "La fecha u hora no existe");
        }
    }
}

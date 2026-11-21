package com.appointy.appointment.domain.usecase;

import com.appointy.appointment.domain.gateway.GeneradorIdGateway;
import com.appointy.appointment.domain.gateway.Operacion;
import com.appointy.appointment.domain.gateway.RegistroGateway;
import com.appointy.appointment.domain.gateway.RelojGateway;
import com.appointy.appointment.domain.gateway.TransactionGateway;
import com.appointy.appointment.domain.rule.FechaIso;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/** Dobles de prueba para los puertos de sistema: reloj fijo, ids reales, transaccion directa y registro mudo. */
final class Dobles {

    static final TransactionGateway TX = new TransactionGateway() {
        @Override
        public <T> T ejecutar(Operacion<T> operacion) {
            return operacion.ejecutar();
        }

        @Override
        public <T> T ejecutarSoloLectura(Operacion<T> operacion) {
            return operacion.ejecutar();
        }
    };

    static final GeneradorIdGateway IDS = () -> UUID.randomUUID().toString();

    static final RegistroGateway REGISTRO = new RegistroGateway() {
        @Override
        public void info(String mensaje) {}

        @Override
        public void error(String mensaje) {}
    };

    private Dobles() {}

    /** Reloj fijo de la clinica: instante UTC y zona horaria local. */
    static RelojGateway reloj(String instanteIso, String zona) {
        Clock clock = Clock.fixed(Instant.parse(instanteIso), ZoneId.of(zona));
        return new RelojGateway() {
            @Override
            public long ahoraMillis() {
                return clock.millis();
            }

            @Override
            public String hoy() {
                return LocalDate.now(clock).toString();
            }

            @Override
            public long ahoraMinutosLocales() {
                LocalDateTime ahora = LocalDateTime.now(clock);
                return FechaIso.minutosLocales(ahora.toLocalDate().toString(), ahora.getHour() * 60 + ahora.getMinute());
            }
        };
    }
}

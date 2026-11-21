package com.appointy.notification.domain.usecase;

import com.appointy.notification.domain.gateway.GeneradorIdGateway;
import com.appointy.notification.domain.gateway.Operacion;
import com.appointy.notification.domain.gateway.RegistroGateway;
import com.appointy.notification.domain.gateway.RelojGateway;
import com.appointy.notification.domain.gateway.TransactionGateway;
import com.appointy.notification.domain.exception.ValidacionException;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
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

    /** Reloj fijo con la zona horaria de la clinica. */
    static RelojGateway reloj(Clock clock, ZoneId zona) {
        return new RelojGateway() {
            @Override
            public long ahora() {
                return clock.millis();
            }

            @Override
            public long inicioEnMillis(String fecha, String hora) {
                try {
                    return LocalDate.parse(fecha).atTime(LocalTime.parse(hora)).atZone(zona).toInstant().toEpochMilli();
                } catch (DateTimeException e) {
                    throw new ValidacionException("FECHA_INVALIDA", "La fecha u hora no existe");
                }
            }
        };
    }
}

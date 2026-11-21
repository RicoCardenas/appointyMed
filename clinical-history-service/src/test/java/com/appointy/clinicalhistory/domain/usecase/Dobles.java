package com.appointy.clinicalhistory.domain.usecase;

import com.appointy.clinicalhistory.domain.gateway.GeneradorIdGateway;
import com.appointy.clinicalhistory.domain.gateway.Operacion;
import com.appointy.clinicalhistory.domain.gateway.RegistroGateway;
import com.appointy.clinicalhistory.domain.gateway.RelojGateway;
import com.appointy.clinicalhistory.domain.gateway.TransactionGateway;
import java.time.Instant;
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

    /** Reloj fijo en los milisegundos dados. */
    static RelojGateway relojEnMillis(long millis) {
        return reloj(Instant.ofEpochMilli(millis).toString());
    }

    /** Reloj fijo en el instante ISO dado (UTC). */
    static RelojGateway reloj(String instanteIso) {
        long millis = Instant.parse(instanteIso).toEpochMilli();
        String hoy = instanteIso.substring(0, 10);
        return new RelojGateway() {
            @Override
            public long ahora() {
                return millis;
            }

            @Override
            public String hoy() {
                return hoy;
            }
        };
    }
}

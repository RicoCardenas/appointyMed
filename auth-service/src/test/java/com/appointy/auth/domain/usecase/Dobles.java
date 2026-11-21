package com.appointy.auth.domain.usecase;

import com.appointy.auth.domain.gateway.GeneradorIdGateway;
import com.appointy.auth.domain.gateway.Operacion;
import com.appointy.auth.domain.gateway.RegistroGateway;
import com.appointy.auth.domain.gateway.RelojGateway;
import com.appointy.auth.domain.gateway.TransactionGateway;
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

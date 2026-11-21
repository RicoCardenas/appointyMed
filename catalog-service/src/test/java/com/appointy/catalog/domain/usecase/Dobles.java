package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.gateway.GeneradorIdGateway;
import com.appointy.catalog.domain.gateway.Operacion;
import com.appointy.catalog.domain.gateway.RegistroGateway;
import com.appointy.catalog.domain.gateway.RelojGateway;
import com.appointy.catalog.domain.gateway.TransactionGateway;
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
        return reloj(instanteIso, instanteIso.substring(0, 10));
    }

    /** Reloj fijo con la fecha local indicada (zona distinta de UTC). */
    static RelojGateway reloj(String instanteIso, String hoy) {
        long millis = Instant.parse(instanteIso).toEpochMilli();
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

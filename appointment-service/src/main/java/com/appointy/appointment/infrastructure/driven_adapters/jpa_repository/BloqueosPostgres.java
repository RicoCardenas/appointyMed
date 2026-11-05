package com.appointy.appointment.infrastructure.driven_adapters.jpa_repository;

import com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.repository.SpringDataBloqueoPgRepository;
import org.springframework.stereotype.Component;

/** Espera maxima de bloqueo y candado por paciente, ambos de la transaccion en curso. */
@Component
class BloqueosPostgres {

    private static final String ESPERA_MAXIMA_MS = "5000";

    private final SpringDataBloqueoPgRepository repositorio;

    BloqueosPostgres(SpringDataBloqueoPgRepository repositorio) {
        this.repositorio = repositorio;
    }

    void limitarEspera() {
        repositorio.fijarEsperaMaxima(ESPERA_MAXIMA_MS);
    }

    void bloquearClave(String clave) {
        limitarEspera();
        repositorio.bloquearClave(clave);
    }
}

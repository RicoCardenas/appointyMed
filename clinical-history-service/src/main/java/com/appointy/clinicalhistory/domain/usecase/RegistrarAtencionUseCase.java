package com.appointy.clinicalhistory.domain.usecase;

import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.ContenidoClinico;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;

/** CU-28: el profesional de la cita registra la atencion. Cita CONFIRMADA o COMPLETADA; una sola entrada original por cita. */
public interface RegistrarAtencionUseCase {

    record Comando(Actor actor, String citaId, ContenidoClinico contenido) {}

    EntradaClinica ejecutar(Comando comando);
}

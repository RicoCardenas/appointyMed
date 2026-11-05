package com.appointy.clinicalhistory.domain.gateway;

import com.appointy.clinicalhistory.domain.model.EntradaClinica;

/** Solo inserta y lee: no hay actualizacion ni borrado (RF-29). */
public interface EntradaClinicaRepositoryGateway {

    void registrar(EntradaClinica entrada);

    /** Devuelve null si no existe. */
    EntradaClinica buscarPorId(String id);

    /** Todas las entradas del paciente por fecha ascendente. */
    EntradaClinica[] listarDePaciente(String pacienteId);

    /** Hay una entrada original (no correccion) para esa cita. */
    boolean existeOriginalDeCita(String citaId);
}

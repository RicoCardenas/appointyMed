package com.appointy.appointment.domain.usecase;

/** CU-27: el profesional (id de catalog) ha tenido o tiene una cita con el paciente. */
public interface ValidarRelacionUseCase {

    boolean ejecutar(String profesionalId, String pacienteId);
}

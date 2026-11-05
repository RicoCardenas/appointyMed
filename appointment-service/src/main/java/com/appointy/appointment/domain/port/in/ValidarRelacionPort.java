package com.appointy.appointment.domain.port.in;

/** CU-27: el profesional (id de catalog) ha tenido o tiene una cita con el paciente. */
public interface ValidarRelacionPort {

    boolean ejecutar(String profesionalId, String pacienteId);
}

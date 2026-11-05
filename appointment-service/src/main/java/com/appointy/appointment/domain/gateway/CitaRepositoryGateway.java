package com.appointy.appointment.domain.gateway;

import com.appointy.appointment.domain.model.Cita;

public interface CitaRepositoryGateway {

    /** Inserta la cita si su version es null; si no, la actualiza. */
    Cita guardar(Cita cita);

    /** Devuelve null si no existe. */
    Cita buscarPorId(String id);

    /** Candado de la transaccion por paciente, para que dos reservas suyas en franjas distintas no se crucen. */
    void bloquearPaciente(String pacienteId);

    /** Citas del paciente cuya franja cae entre dos fechas ISO inclusive, por fecha y hora ascendentes. */
    Cita[] listarDePaciente(String pacienteId, String desde, String hasta);

    /** Citas CONFIRMADAS del profesional entre dos fechas ISO inclusive, por fecha y hora ascendentes. */
    Cita[] listarAgendaConfirmada(String profesionalId, String desde, String hasta);

    /** Ids de las citas CONFIRMADAS del profesional en el centro con franja entre dos fechas ISO inclusive. */
    String[] idsConfirmadasEnRango(String profesionalId, String centroId, String fechaInicio, String fechaFin);

    /** Hay alguna cita CONFIRMADA, COMPLETADA o NO_ASISTIDA entre ese profesional y ese paciente. */
    boolean existeRelacion(String profesionalId, String pacienteId);
}

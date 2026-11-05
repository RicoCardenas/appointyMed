package com.appointy.appointment.domain.gateway;

import com.appointy.appointment.domain.model.Franja;

public interface FranjaRepositoryGateway {

    /** Devuelve null si no existe. */
    Franja buscarPorId(String id);

    /** Lee la franja con bloqueo pesimista de escritura (PESSIMISTIC_WRITE). Devuelve null si no existe. Solo dentro de una transaccion. */
    Franja buscarParaActualizar(String id);

    /** Bloquea y devuelve las franjas del profesional en el centro entre dos fechas ISO inclusive, en orden ascendente de id. */
    Franja[] bloquearRango(String profesionalId, String centroId, String fechaInicio, String fechaFin);

    Franja[] buscarPorIds(String[] ids);

    /** Franjas DISPONIBLES de ese dia, por hora de inicio. */
    Franja[] listarDisponibles(String profesionalId, String centroId, String fecha);

    /** Franjas de las citas CONFIRMADAS del paciente en esa fecha ISO. */
    Franja[] franjasDeCitasConfirmadasDelPaciente(String pacienteId, String fecha);

    Franja guardar(Franja franja);

    /** Inserta la franja salvo que ya exista (profesional, centro, fecha, hora de inicio). Devuelve true si la creo. */
    boolean insertarSiNoExiste(Franja franja);
}

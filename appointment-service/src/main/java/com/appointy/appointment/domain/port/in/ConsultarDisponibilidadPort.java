package com.appointy.appointment.domain.port.in;

import com.appointy.appointment.domain.model.Franja;

/** CU-17: franjas DISPONIBLES y futuras de un profesional en un centro y fecha (ISO). */
public interface ConsultarDisponibilidadPort {

    Franja[] ejecutar(String profesionalId, String centroId, String fecha);
}

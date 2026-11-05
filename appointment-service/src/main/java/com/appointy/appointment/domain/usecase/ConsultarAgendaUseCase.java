package com.appointy.appointment.domain.usecase;

import com.appointy.appointment.domain.model.CitaConFranja;

/** CU-22: citas CONFIRMADAS del profesional. Con {@code fecha} (ISO) solo ese dia; con null, de hoy en adelante. */
public interface ConsultarAgendaUseCase {

    CitaConFranja[] ejecutar(String usuarioId, String fecha);
}

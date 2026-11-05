package com.appointy.appointment.domain.usecase;

import com.appointy.appointment.domain.model.CitaConFranja;

/** CU-21. {@code periodo}: PROXIMAS, PASADAS o null para todas. Proximas de la mas cercana a la mas lejana; el resto, de la mas reciente a la mas antigua. */
public interface ConsultarMisCitasUseCase {

    CitaConFranja[] ejecutar(String pacienteId, String periodo);
}

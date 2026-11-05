package com.appointy.appointment.domain.port.in;

import com.appointy.appointment.domain.model.CitaConFranja;

/** CU-21. {@code periodo}: PROXIMAS, PASADAS o null para todas. Proximas de la mas cercana a la mas lejana; el resto, de la mas reciente a la mas antigua. */
public interface ConsultarMisCitasPort {

    CitaConFranja[] ejecutar(String pacienteId, String periodo);
}

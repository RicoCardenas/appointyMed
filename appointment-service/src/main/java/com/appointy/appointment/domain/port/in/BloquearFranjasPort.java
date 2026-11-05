package com.appointy.appointment.domain.port.in;

/** CU-26: devuelve cuantas franjas pasaron a BLOQUEADA; lanza ConflictoCitasException si hay citas CONFIRMADAS. Idempotente. */
public interface BloquearFranjasPort {

    int ejecutar(String profesionalId, String centroId, String fechaInicio, String fechaFin);
}

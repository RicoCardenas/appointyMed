package com.appointy.appointment.domain.usecase;

/** CU-26: devuelve cuantas franjas pasaron a BLOQUEADA; lanza ConflictoCitasException si hay citas CONFIRMADAS. Idempotente. */
public interface BloquearFranjasUseCase {

    int ejecutar(String profesionalId, String centroId, String fechaInicio, String fechaFin);
}

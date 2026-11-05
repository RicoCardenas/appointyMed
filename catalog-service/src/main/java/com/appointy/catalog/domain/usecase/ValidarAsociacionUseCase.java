package com.appointy.catalog.domain.usecase;

/** Consulta interna de appointment (CU-18): profesional activo y asociado a la especialidad y al centro. */
public interface ValidarAsociacionUseCase {

    Resultado ejecutar(String profesionalId, String especialidadId, String centroId);

    record Resultado(boolean profesionalActivo, boolean asociado) {}
}

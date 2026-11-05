package com.appointy.catalog.domain.port.in;

/** Consulta interna de appointment (CU-18): profesional activo y asociado a la especialidad y al centro. */
public interface ValidarAsociacionPort {

    Resultado ejecutar(String profesionalId, String especialidadId, String centroId);

    record Resultado(boolean profesionalActivo, boolean asociado) {}
}

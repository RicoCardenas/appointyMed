package com.appointy.catalog.domain.port.in;

/** CU-11: borrado logico; la especialidad deja de ofrecerse. */
public interface DesactivarEspecialidadPort {

    void ejecutar(String id);
}

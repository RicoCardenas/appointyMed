package com.appointy.catalog.domain.usecase;

/** CU-11: borrado logico; la especialidad deja de ofrecerse. */
public interface DesactivarEspecialidadUseCase {

    void ejecutar(String id);
}

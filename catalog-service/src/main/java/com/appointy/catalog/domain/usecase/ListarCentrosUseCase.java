package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.model.Centro;

/** Todos los centros por nombre; con {@code incluirInactivos} false, solo los activos. */
public interface ListarCentrosUseCase {

    Centro[] ejecutar(boolean incluirInactivos);
}

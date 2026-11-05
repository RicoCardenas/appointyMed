package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.model.Especialidad;
import com.appointy.catalog.domain.usecase.CrearEspecialidadUseCase;
import com.appointy.catalog.domain.gateway.EspecialidadRepositoryGateway;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class CrearEspecialidadUseCaseImpl implements CrearEspecialidadUseCase {

    private final EspecialidadRepositoryGateway especialidades;

    public CrearEspecialidadUseCaseImpl(EspecialidadRepositoryGateway especialidades) {
        this.especialidades = especialidades;
    }

    @Override
    @Transactional
    public Especialidad ejecutar(Comando c) {
        if (c.nombre() == null || c.nombre().isBlank()) {
            throw new ValidacionException("NOMBRE_REQUERIDO", "El nombre de la especialidad es obligatorio");
        }
        String nombre = c.nombre().trim();
        if (especialidades.buscarPorNombre(nombre) != null) {
            throw new ConflictoException("ESPECIALIDAD_YA_EXISTE", "Ya existe una especialidad con ese nombre");
        }
        return especialidades.guardar(new Especialidad(UUID.randomUUID().toString(), nombre, c.descripcion(), true));
    }
}

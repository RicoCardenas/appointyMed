package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.model.Especialidad;
import com.appointy.catalog.domain.port.in.CrearEspecialidadPort;
import com.appointy.catalog.domain.port.out.EspecialidadRepositoryPort;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class CrearEspecialidadUseCase implements CrearEspecialidadPort {

    private final EspecialidadRepositoryPort especialidades;

    public CrearEspecialidadUseCase(EspecialidadRepositoryPort especialidades) {
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

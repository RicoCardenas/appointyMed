package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.model.Especialidad;
import com.appointy.catalog.domain.port.in.ActualizarEspecialidadPort;
import com.appointy.catalog.domain.port.out.EspecialidadRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class ActualizarEspecialidadUseCase implements ActualizarEspecialidadPort {

    private final EspecialidadRepositoryPort especialidades;

    public ActualizarEspecialidadUseCase(EspecialidadRepositoryPort especialidades) {
        this.especialidades = especialidades;
    }

    @Override
    @Transactional
    public Especialidad ejecutar(Comando c) {
        if (c.nombre() == null || c.nombre().isBlank()) {
            throw new ValidacionException("NOMBRE_REQUERIDO", "El nombre de la especialidad es obligatorio");
        }
        Especialidad actual = especialidades.buscarPorId(c.id());
        if (actual == null) {
            throw new NoEncontradoException("ESPECIALIDAD_NO_ENCONTRADA", "La especialidad no existe");
        }
        String nombre = c.nombre().trim();
        Especialidad homonima = especialidades.buscarPorNombre(nombre);
        if (homonima != null && !homonima.id().equals(actual.id())) {
            throw new ConflictoException("ESPECIALIDAD_YA_EXISTE", "Ya existe una especialidad con ese nombre");
        }
        return especialidades.guardar(actual.actualizar(nombre, c.descripcion()));
    }
}

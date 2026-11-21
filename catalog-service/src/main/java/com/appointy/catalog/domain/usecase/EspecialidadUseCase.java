package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.gateway.GeneradorIdGateway;
import com.appointy.catalog.domain.gateway.TransactionGateway;
import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.gateway.EspecialidadRepositoryGateway;
import com.appointy.catalog.domain.model.Especialidad;

/** crear especialidad, actualizar especialidad, desactivar especialidad, listar especialidades. */
public class EspecialidadUseCase {

    public record CrearEspecialidadComando(String nombre, String descripcion) {}

    public record ActualizarEspecialidadComando(String id, String nombre, String descripcion) {}

    private final EspecialidadRepositoryGateway especialidades;
    private final TransactionGateway transacciones;
    private final GeneradorIdGateway ids;

    public EspecialidadUseCase(EspecialidadRepositoryGateway especialidades, TransactionGateway transacciones, GeneradorIdGateway ids) {
        this.ids = ids;
        this.transacciones = transacciones;
        this.especialidades = especialidades;
    }

    /** CU-11: crea una especialidad activa. */
    public Especialidad crearEspecialidad(CrearEspecialidadComando c) {
        return transacciones.ejecutar(() -> {
            if (c.nombre() == null || c.nombre().isBlank()) {
                throw new ValidacionException("NOMBRE_REQUERIDO", "El nombre de la especialidad es obligatorio");
            }
            String nombre = c.nombre().trim();
            if (especialidades.buscarPorNombre(nombre) != null) {
                throw new ConflictoException("ESPECIALIDAD_YA_EXISTE", "Ya existe una especialidad con ese nombre");
            }
            return especialidades.guardar(new Especialidad(ids.nuevoId(), nombre, c.descripcion(), true));
        });
    }

    /** CU-11: edita nombre y descripcion. */
    public Especialidad actualizarEspecialidad(ActualizarEspecialidadComando c) {
        return transacciones.ejecutar(() -> {
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
        });
    }

    /** CU-11: borrado logico; la especialidad deja de ofrecerse. */
    public void desactivarEspecialidad(String id) {
        transacciones.ejecutar(() -> {
            Especialidad actual = especialidades.buscarPorId(id);
            if (actual == null) {
                throw new NoEncontradoException("ESPECIALIDAD_NO_ENCONTRADA", "La especialidad no existe");
            }
            if (actual.activo()) {
                especialidades.guardar(actual.desactivar());
            }
    
            return null;
        });
    }

    /** CU-08: especialidades disponibles; los administrativos pueden pedir tambien las inactivas. */
    public Especialidad[] listarEspecialidades(boolean incluirInactivas) {
        return transacciones.ejecutarSoloLectura(() -> {
            return especialidades.listar(incluirInactivas);
        });
    }
}

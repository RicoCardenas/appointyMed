package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.gateway.GeneradorIdGateway;
import com.appointy.catalog.domain.gateway.TransactionGateway;
import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.gateway.CentroRepositoryGateway;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import com.appointy.catalog.domain.model.Centro;

/** crear centro, actualizar centro, desactivar centro, listar centros, listar centros de profesional. */
public class CentroUseCase {

    public record CrearCentroComando(String nombre, String direccion, String ciudad, String telefono) {}

    public record ActualizarCentroComando(String id, String nombre, String direccion, String ciudad, String telefono) {}

    private final CentroRepositoryGateway centros;
    private final ProfesionalRepositoryGateway profesionales;
    private final TransactionGateway transacciones;
    private final GeneradorIdGateway ids;

    public CentroUseCase(CentroRepositoryGateway centros, ProfesionalRepositoryGateway profesionales, TransactionGateway transacciones, GeneradorIdGateway ids) {
        this.ids = ids;
        this.transacciones = transacciones;
        this.centros = centros;
        this.profesionales = profesionales;
    }

    /** CU-11: crea un centro de atencion activo. */
    public Centro crearCentro(CrearCentroComando c) {
        return transacciones.ejecutar(() -> {
            return centros.guardar(new Centro(ids.nuevoId(), c.nombre().trim(), c.direccion().trim(), c.ciudad().trim(), c.telefono(), true));
        });
    }

    /** CU-11: edita un centro. */
    public Centro actualizarCentro(ActualizarCentroComando c) {
        return transacciones.ejecutar(() -> {
            Centro actual = centros.buscarPorId(c.id());
            if (actual == null) {
                throw new NoEncontradoException("CENTRO_NO_ENCONTRADO", "El centro no existe");
            }
            return centros.guardar(actual.actualizar(c.nombre().trim(), c.direccion().trim(), c.ciudad().trim(), c.telefono()));
        });
    }

    /** CU-11: borrado logico del centro. */
    public void desactivarCentro(String id) {
        transacciones.ejecutar(() -> {
            Centro actual = centros.buscarPorId(id);
            if (actual == null) {
                throw new NoEncontradoException("CENTRO_NO_ENCONTRADO", "El centro no existe");
            }
            if (actual.activo()) {
                centros.guardar(actual.desactivar());
            }
    
            return null;
        });
    }

    /** Todos los centros por nombre; con {@code incluirInactivos} false, solo los activos. */
    public Centro[] listarCentros(boolean incluirInactivos) {
        return transacciones.ejecutarSoloLectura(() -> {
            return centros.listar(incluirInactivos);
        });
    }

    /** CU-10: centros activos donde atiende el profesional. */
    public Centro[] listarCentrosDeProfesional(String profesionalId) {
        return transacciones.ejecutarSoloLectura(() -> {
            if (profesionales.buscarPorId(profesionalId) == null) {
                throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "El profesional no existe");
            }
            return centros.listarActivosDeProfesional(profesionalId);
        });
    }
}

package com.appointy.catalog.domain.gateway;

import com.appointy.catalog.domain.model.FiltroProfesionales;
import com.appointy.catalog.domain.model.PaginaProfesionales;
import com.appointy.catalog.domain.model.Profesional;

public interface ProfesionalRepositoryGateway {

    Profesional guardar(Profesional profesional);

    /** Devuelve null si no existe. */
    Profesional buscarPorId(String id);

    /** Devuelve null si no existe. */
    Profesional buscarPorUsuarioId(String usuarioId);

    /** Devuelve null si no existe. */
    Profesional buscarPorRegistroMedico(String registroMedico);

    Profesional[] listarActivosPorEspecialidad(String especialidadId);

    /** Listado paginado ordenado por apellidos y nombres. {@code pagina} empieza en 0. */
    PaginaProfesionales listar(FiltroProfesionales filtro, int pagina, int tamano);

    String[] idsEspecialidades(String profesionalId);

    String[] idsCentros(String profesionalId);

    void reemplazarEspecialidades(String profesionalId, String[] especialidadIds);

    void reemplazarCentros(String profesionalId, String[] centroIds);

    boolean atiendeEnCentro(String profesionalId, String centroId);

    boolean tieneEspecialidad(String profesionalId, String especialidadId);
}

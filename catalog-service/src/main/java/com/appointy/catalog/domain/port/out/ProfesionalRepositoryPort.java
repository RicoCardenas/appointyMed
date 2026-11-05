package com.appointy.catalog.domain.port.out;

import com.appointy.catalog.domain.model.Profesional;

public interface ProfesionalRepositoryPort {

    Profesional guardar(Profesional profesional);

    /** Devuelve null si no existe. */
    Profesional buscarPorId(String id);

    /** Devuelve null si no existe. */
    Profesional buscarPorUsuarioId(String usuarioId);

    /** Devuelve null si no existe. */
    Profesional buscarPorRegistroMedico(String registroMedico);

    Profesional[] listarActivosPorEspecialidad(String especialidadId);

    String[] idsEspecialidades(String profesionalId);

    String[] idsCentros(String profesionalId);

    void reemplazarEspecialidades(String profesionalId, String[] especialidadIds);

    void reemplazarCentros(String profesionalId, String[] centroIds);

    boolean atiendeEnCentro(String profesionalId, String centroId);

    boolean tieneEspecialidad(String profesionalId, String especialidadId);
}

package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.model.DetalleProfesional;
import com.appointy.catalog.domain.model.Especialidad;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.port.in.AsociarEspecialidadesPort;
import com.appointy.catalog.domain.port.out.EspecialidadRepositoryPort;
import com.appointy.catalog.domain.port.out.ProfesionalRepositoryPort;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

public class AsociarEspecialidadesUseCase implements AsociarEspecialidadesPort {

    private final ProfesionalRepositoryPort profesionales;
    private final EspecialidadRepositoryPort especialidades;

    public AsociarEspecialidadesUseCase(ProfesionalRepositoryPort profesionales, EspecialidadRepositoryPort especialidades) {
        this.profesionales = profesionales;
        this.especialidades = especialidades;
    }

    @Override
    @Transactional
    public DetalleProfesional ejecutar(Comando c) {
        Profesional p = profesionales.buscarPorId(c.profesionalId());
        if (p == null) {
            throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "El profesional no existe");
        }
        Set<String> ids = new LinkedHashSet<>(java.util.Arrays.asList(c.especialidadIds()));
        for (String id : ids) {
            Especialidad e = especialidades.buscarPorId(id);
            if (e == null) {
                throw new NoEncontradoException("ESPECIALIDAD_NO_ENCONTRADA", "La especialidad " + id + " no existe");
            }
            if (!e.activo()) {
                throw new ValidacionException("ESPECIALIDAD_INACTIVA", "La especialidad " + id + " esta inactiva");
            }
        }
        String[] nuevas = ids.toArray(new String[0]);
        profesionales.reemplazarEspecialidades(p.id(), nuevas);
        return new DetalleProfesional(p, nuevas, profesionales.idsCentros(p.id()));
    }
}

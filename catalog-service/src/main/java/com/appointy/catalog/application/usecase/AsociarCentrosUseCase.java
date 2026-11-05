package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.model.DetalleProfesional;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.port.in.AsociarCentrosPort;
import com.appointy.catalog.domain.port.out.BloqueoRepositoryPort;
import com.appointy.catalog.domain.port.out.CentroRepositoryPort;
import com.appointy.catalog.domain.port.out.HorarioRepositoryPort;
import com.appointy.catalog.domain.port.out.ProfesionalRepositoryPort;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

public class AsociarCentrosUseCase implements AsociarCentrosPort {

    private final ProfesionalRepositoryPort profesionales;
    private final CentroRepositoryPort centros;
    private final HorarioRepositoryPort horarios;
    private final BloqueoRepositoryPort bloqueos;

    public AsociarCentrosUseCase(
            ProfesionalRepositoryPort profesionales, CentroRepositoryPort centros, HorarioRepositoryPort horarios, BloqueoRepositoryPort bloqueos) {
        this.profesionales = profesionales;
        this.centros = centros;
        this.horarios = horarios;
        this.bloqueos = bloqueos;
    }

    @Override
    @Transactional
    public DetalleProfesional ejecutar(Comando c) {
        Profesional p = profesionales.buscarPorId(c.profesionalId());
        if (p == null) {
            throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "El profesional no existe");
        }
        Set<String> nuevos = new LinkedHashSet<>(Arrays.asList(c.centroIds()));
        for (String id : nuevos) {
            Centro centro = centros.buscarPorId(id);
            if (centro == null) {
                throw new NoEncontradoException("CENTRO_NO_ENCONTRADO", "El centro " + id + " no existe");
            }
            if (!centro.activo()) {
                throw new ValidacionException("CENTRO_INACTIVO", "El centro " + id + " esta inactivo");
            }
        }
        for (String actual : profesionales.idsCentros(p.id())) {
            if (!nuevos.contains(actual)
                    && (horarios.contarPorProfesionalYCentro(p.id(), actual) > 0 || bloqueos.contarPorProfesionalYCentro(p.id(), actual) > 0)) {
                throw new ConflictoException("CENTRO_CON_HORARIOS", "No se puede quitar el centro " + actual + ": tiene horarios o bloqueos del profesional");
            }
        }
        String[] ids = nuevos.toArray(new String[0]);
        profesionales.reemplazarCentros(p.id(), ids);
        return new DetalleProfesional(p, profesionales.idsEspecialidades(p.id()), ids);
    }
}

package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.DetalleProfesional;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.port.in.ConsultarProfesionalPort;
import com.appointy.catalog.domain.port.out.ProfesionalRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class ConsultarProfesionalUseCase implements ConsultarProfesionalPort {

    private final ProfesionalRepositoryPort profesionales;

    public ConsultarProfesionalUseCase(ProfesionalRepositoryPort profesionales) {
        this.profesionales = profesionales;
    }

    @Override
    @Transactional(readOnly = true)
    public DetalleProfesional ejecutar(String profesionalId) {
        Profesional p = profesionales.buscarPorId(profesionalId);
        if (p == null) {
            throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "El profesional no existe");
        }
        return new DetalleProfesional(p, profesionales.idsEspecialidades(profesionalId), profesionales.idsCentros(profesionalId));
    }
}

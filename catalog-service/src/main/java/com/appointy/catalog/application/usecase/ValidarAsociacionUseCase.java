package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.port.in.ValidarAsociacionPort;
import com.appointy.catalog.domain.port.out.ProfesionalRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class ValidarAsociacionUseCase implements ValidarAsociacionPort {

    private final ProfesionalRepositoryPort profesionales;

    public ValidarAsociacionUseCase(ProfesionalRepositoryPort profesionales) {
        this.profesionales = profesionales;
    }

    @Override
    @Transactional(readOnly = true)
    public Resultado ejecutar(String profesionalId, String especialidadId, String centroId) {
        Profesional p = profesionales.buscarPorId(profesionalId);
        if (p == null) {
            return new Resultado(false, false);
        }
        boolean asociado = profesionales.tieneEspecialidad(profesionalId, especialidadId) && profesionales.atiendeEnCentro(profesionalId, centroId);
        return new Resultado(p.activo(), asociado);
    }
}

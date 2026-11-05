package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.port.in.ActualizarProfesionalPort;
import com.appointy.catalog.domain.port.out.ProfesionalRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class ActualizarProfesionalUseCase implements ActualizarProfesionalPort {

    private final ProfesionalRepositoryPort profesionales;

    public ActualizarProfesionalUseCase(ProfesionalRepositoryPort profesionales) {
        this.profesionales = profesionales;
    }

    @Override
    @Transactional
    public Profesional ejecutar(Comando c) {
        Profesional actual = profesionales.buscarPorId(c.id());
        if (actual == null) {
            throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "El profesional no existe");
        }
        String registro = c.registroMedico().trim();
        Profesional conRegistro = profesionales.buscarPorRegistroMedico(registro);
        if (conRegistro != null && !conRegistro.id().equals(actual.id())) {
            throw new ConflictoException("REGISTRO_MEDICO_YA_REGISTRADO", "El registro medico ya esta registrado");
        }
        return profesionales.guardar(actual.actualizar(c.nombres().trim(), c.apellidos().trim(), registro));
    }
}

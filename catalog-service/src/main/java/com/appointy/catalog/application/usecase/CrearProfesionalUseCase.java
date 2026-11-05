package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.port.in.CrearProfesionalPort;
import com.appointy.catalog.domain.port.out.ProfesionalRepositoryPort;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class CrearProfesionalUseCase implements CrearProfesionalPort {

    private final ProfesionalRepositoryPort profesionales;

    public CrearProfesionalUseCase(ProfesionalRepositoryPort profesionales) {
        this.profesionales = profesionales;
    }

    @Override
    @Transactional
    public Profesional ejecutar(Comando c) {
        String registro = c.registroMedico().trim();
        if (profesionales.buscarPorUsuarioId(c.usuarioId()) != null) {
            throw new ConflictoException("PROFESIONAL_YA_EXISTE", "Esa cuenta ya tiene un perfil de profesional");
        }
        if (profesionales.buscarPorRegistroMedico(registro) != null) {
            throw new ConflictoException("REGISTRO_MEDICO_YA_REGISTRADO", "El registro medico ya esta registrado");
        }
        return profesionales.guardar(new Profesional(UUID.randomUUID().toString(), c.usuarioId(), c.nombres().trim(), c.apellidos().trim(), registro, true));
    }
}

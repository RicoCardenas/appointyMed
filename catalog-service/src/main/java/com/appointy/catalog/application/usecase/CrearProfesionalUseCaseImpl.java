package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.usecase.CrearProfesionalUseCase;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class CrearProfesionalUseCaseImpl implements CrearProfesionalUseCase {

    private final ProfesionalRepositoryGateway profesionales;

    public CrearProfesionalUseCaseImpl(ProfesionalRepositoryGateway profesionales) {
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

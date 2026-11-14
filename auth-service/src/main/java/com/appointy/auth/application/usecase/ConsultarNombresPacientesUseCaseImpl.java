package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.gateway.PacientePerfilRepositoryGateway;
import com.appointy.auth.domain.model.NombrePaciente;
import com.appointy.auth.domain.model.PacientePerfil;
import com.appointy.auth.domain.rule.ReglasUsuarios;
import com.appointy.auth.domain.usecase.ConsultarNombresPacientesUseCase;
import org.springframework.transaction.annotation.Transactional;

public class ConsultarNombresPacientesUseCaseImpl implements ConsultarNombresPacientesUseCase {

    private final PacientePerfilRepositoryGateway perfiles;

    public ConsultarNombresPacientesUseCaseImpl(PacientePerfilRepositoryGateway perfiles) {
        this.perfiles = perfiles;
    }

    @Override
    @Transactional(readOnly = true)
    public NombrePaciente[] ejecutar(String[] usuarioIds) {
        ReglasUsuarios.validarLoteDeIds(usuarioIds);
        PacientePerfil[] encontrados = perfiles.buscarPorUsuarioIds(usuarioIds);
        NombrePaciente[] nombres = new NombrePaciente[encontrados.length];
        for (int i = 0; i < encontrados.length; i++) {
            nombres[i] = new NombrePaciente(encontrados[i].usuarioId(), encontrados[i].nombres(), encontrados[i].apellidos());
        }
        return nombres;
    }
}

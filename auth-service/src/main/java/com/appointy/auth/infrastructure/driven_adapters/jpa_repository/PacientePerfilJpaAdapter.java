package com.appointy.auth.infrastructure.driven_adapters.jpa_repository;

import com.appointy.auth.domain.model.PacientePerfil;
import com.appointy.auth.domain.port.out.PacientePerfilRepositoryPort;
import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.repository.SpringDataPacientePerfilRepository;
import com.appointy.auth.infrastructure.mapper.PacientePerfilEntityMapper;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PacientePerfilJpaAdapter implements PacientePerfilRepositoryPort {

    private final SpringDataPacientePerfilRepository repositorio;
    private final PacientePerfilEntityMapper mapper;

    public PacientePerfilJpaAdapter(SpringDataPacientePerfilRepository repositorio, PacientePerfilEntityMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public PacientePerfil guardar(PacientePerfil perfil) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(perfil)));
    }

    @Override
    public PacientePerfil buscarPorUsuarioId(String usuarioId) {
        return repositorio.findById(UUID.fromString(usuarioId)).map(mapper::aDominio).orElse(null);
    }

    @Override
    public PacientePerfil buscarPorCedula(String cedula) {
        return repositorio.findByCedula(cedula).map(mapper::aDominio).orElse(null);
    }
}

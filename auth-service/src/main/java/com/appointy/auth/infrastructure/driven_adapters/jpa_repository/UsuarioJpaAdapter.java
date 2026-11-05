package com.appointy.auth.infrastructure.driven_adapters.jpa_repository;

import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.port.out.UsuarioRepositoryPort;
import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.repository.SpringDataUsuarioRepository;
import com.appointy.auth.infrastructure.mapper.UsuarioEntityMapper;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UsuarioJpaAdapter implements UsuarioRepositoryPort {

    private final SpringDataUsuarioRepository repositorio;
    private final UsuarioEntityMapper mapper;

    public UsuarioJpaAdapter(SpringDataUsuarioRepository repositorio, UsuarioEntityMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(usuario)));
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        return repositorio.findById(id).map(mapper::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return repositorio.findByCorreo(correo).map(mapper::aDominio);
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return repositorio.existsByCorreo(correo);
    }

    @Override
    public boolean existeAlgunoConRol(Rol rol) {
        return repositorio.existsByRol(rol);
    }
}

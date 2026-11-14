package com.appointy.auth.infrastructure.driven_adapters.jpa_repository;

import com.appointy.auth.domain.model.EstadisticasUsuarios;
import com.appointy.auth.domain.model.FiltroUsuarios;
import com.appointy.auth.domain.model.PaginaUsuarios;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.entity.UsuarioEntity;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.repository.SpringDataUsuarioRepository;
import com.appointy.auth.infrastructure.mapper.UsuarioEntityMapper;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UsuarioJpaAdapter implements UsuarioRepositoryGateway {

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
    public Usuario buscarPorId(String id) {
        return repositorio.findById(UUID.fromString(id)).map(mapper::aDominio).orElse(null);
    }

    @Override
    public Usuario buscarPorCorreo(String correo) {
        return repositorio.findByCorreo(correo).map(mapper::aDominio).orElse(null);
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return repositorio.existsByCorreo(correo);
    }

    @Override
    public boolean existeAlgunoConRol(Rol rol) {
        return repositorio.existsByRol(rol);
    }

    @Override
    public PaginaUsuarios listar(FiltroUsuarios filtro, int pagina, int tamano) {
        Specification<UsuarioEntity> spec = (raiz, consulta, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (filtro != null && filtro.rol() != null) condiciones.add(cb.equal(raiz.get("rol"), filtro.rol()));
            if (filtro != null && filtro.activo() != null) condiciones.add(cb.equal(raiz.get("activo"), filtro.activo()));
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
        Page<UsuarioEntity> resultado = repositorio.findAll(spec, PageRequest.of(pagina, tamano, Sort.by(Sort.Order.desc("creadoEn"), Sort.Order.asc("id"))));
        return new PaginaUsuarios(resultado.getContent().stream().map(mapper::aDominio).toArray(Usuario[]::new), pagina, tamano,
                resultado.getTotalElements(), resultado.getTotalPages());
    }

    @Override
    public EstadisticasUsuarios estadisticas() {
        return new EstadisticasUsuarios(repositorio.countByRol(Rol.PACIENTE), repositorio.countByRol(Rol.PROFESIONAL),
                repositorio.countByRol(Rol.ADMINISTRATIVO), repositorio.countByActivo(true), repositorio.countByActivo(false));
    }
}

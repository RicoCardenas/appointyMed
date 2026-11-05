package com.appointy.catalog.infrastructure.driven_adapters.jpa_repository;

import com.appointy.catalog.domain.model.Especialidad;
import com.appointy.catalog.domain.port.out.EspecialidadRepositoryPort;
import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity.EspecialidadEntity;
import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.repository.SpringDataEspecialidadRepository;
import com.appointy.catalog.infrastructure.mapper.EspecialidadEntityMapper;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class EspecialidadJpaAdapter implements EspecialidadRepositoryPort {

    private final SpringDataEspecialidadRepository repositorio;
    private final EspecialidadEntityMapper mapper;

    public EspecialidadJpaAdapter(SpringDataEspecialidadRepository repositorio, EspecialidadEntityMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public Especialidad guardar(Especialidad especialidad) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(especialidad)));
    }

    @Override
    public Especialidad buscarPorId(String id) {
        return repositorio.findById(UUID.fromString(id)).map(mapper::aDominio).orElse(null);
    }

    @Override
    public Especialidad buscarPorNombre(String nombre) {
        EspecialidadEntity e = repositorio.findByNombre(nombre);
        return e == null ? null : mapper.aDominio(e);
    }

    @Override
    public Especialidad[] listar(boolean incluirInactivas) {
        return (incluirInactivas ? repositorio.findAllByOrderByNombre() : repositorio.findByActivoTrueOrderByNombre())
                .stream().map(mapper::aDominio).toArray(Especialidad[]::new);
    }
}

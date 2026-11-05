package com.appointy.catalog.infrastructure.driven_adapters.jpa_repository;

import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.port.out.CentroRepositoryPort;
import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.repository.SpringDataCentroRepository;
import com.appointy.catalog.infrastructure.mapper.CentroEntityMapper;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CentroJpaAdapter implements CentroRepositoryPort {

    private final SpringDataCentroRepository repositorio;
    private final CentroEntityMapper mapper;

    public CentroJpaAdapter(SpringDataCentroRepository repositorio, CentroEntityMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public Centro guardar(Centro centro) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(centro)));
    }

    @Override
    public Centro buscarPorId(String id) {
        return repositorio.findById(UUID.fromString(id)).map(mapper::aDominio).orElse(null);
    }

    @Override
    public Centro[] listarActivosDeProfesional(String profesionalId) {
        return repositorio.listarActivosDeProfesional(UUID.fromString(profesionalId)).stream().map(mapper::aDominio).toArray(Centro[]::new);
    }
}

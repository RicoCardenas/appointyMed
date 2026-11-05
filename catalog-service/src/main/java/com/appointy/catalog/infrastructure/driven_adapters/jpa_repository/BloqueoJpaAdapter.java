package com.appointy.catalog.infrastructure.driven_adapters.jpa_repository;

import com.appointy.catalog.domain.model.Bloqueo;
import com.appointy.catalog.domain.gateway.BloqueoRepositoryGateway;
import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.repository.SpringDataBloqueoRepository;
import com.appointy.catalog.infrastructure.mapper.BloqueoEntityMapper;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class BloqueoJpaAdapter implements BloqueoRepositoryGateway {

    private final SpringDataBloqueoRepository repositorio;
    private final BloqueoEntityMapper mapper;

    public BloqueoJpaAdapter(SpringDataBloqueoRepository repositorio, BloqueoEntityMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public Bloqueo guardar(Bloqueo bloqueo) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(bloqueo)));
    }

    @Override
    public int contarPorProfesionalYCentro(String profesionalId, String centroId) {
        return repositorio.countByProfesionalIdAndCentroId(UUID.fromString(profesionalId), UUID.fromString(centroId));
    }
}

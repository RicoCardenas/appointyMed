package com.appointy.catalog.infrastructure.driven_adapters.jpa_repository;

import com.appointy.catalog.domain.model.HorarioAtencion;
import com.appointy.catalog.domain.port.out.HorarioRepositoryPort;
import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.repository.SpringDataHorarioRepository;
import com.appointy.catalog.infrastructure.mapper.HorarioAtencionEntityMapper;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class HorarioJpaAdapter implements HorarioRepositoryPort {

    private final SpringDataHorarioRepository repositorio;
    private final HorarioAtencionEntityMapper mapper;

    public HorarioJpaAdapter(SpringDataHorarioRepository repositorio, HorarioAtencionEntityMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public HorarioAtencion guardar(HorarioAtencion horario) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(horario)));
    }

    @Override
    public HorarioAtencion buscarPorId(String id) {
        return repositorio.findById(UUID.fromString(id)).map(mapper::aDominio).orElse(null);
    }

    @Override
    public HorarioAtencion[] listarDeProfesional(String profesionalId) {
        return repositorio.findByProfesionalIdOrderByDiaSemanaAscHoraInicioAsc(UUID.fromString(profesionalId))
                .stream().map(mapper::aDominio).toArray(HorarioAtencion[]::new);
    }

    @Override
    public int contarPorProfesionalYCentro(String profesionalId, String centroId) {
        return repositorio.countByProfesionalIdAndCentroId(UUID.fromString(profesionalId), UUID.fromString(centroId));
    }
}

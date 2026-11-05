package com.appointy.catalog.infrastructure.driven_adapters.jpa_repository;

import com.appointy.catalog.domain.model.ParametroServicio;
import com.appointy.catalog.domain.port.out.ParametroRepositoryPort;
import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.repository.SpringDataParametroRepository;
import com.appointy.catalog.infrastructure.mapper.ParametroServicioEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class ParametroJpaAdapter implements ParametroRepositoryPort {

    private final SpringDataParametroRepository repositorio;
    private final ParametroServicioEntityMapper mapper;

    public ParametroJpaAdapter(SpringDataParametroRepository repositorio, ParametroServicioEntityMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public ParametroServicio[] listar() {
        return repositorio.findAllByOrderByClave().stream().map(mapper::aDominio).toArray(ParametroServicio[]::new);
    }

    @Override
    public ParametroServicio buscarPorClave(String clave) {
        return repositorio.findById(clave).map(mapper::aDominio).orElse(null);
    }

    @Override
    public ParametroServicio guardar(ParametroServicio parametro) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(parametro)));
    }
}

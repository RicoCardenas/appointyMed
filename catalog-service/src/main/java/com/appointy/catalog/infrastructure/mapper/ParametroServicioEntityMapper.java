package com.appointy.catalog.infrastructure.mapper;

import com.appointy.catalog.domain.model.ParametroServicio;
import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity.ParametroServicioEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = Conversiones.class)
public interface ParametroServicioEntityMapper {

    ParametroServicio aDominio(ParametroServicioEntity entidad);

    ParametroServicioEntity aEntidad(ParametroServicio dominio);
}

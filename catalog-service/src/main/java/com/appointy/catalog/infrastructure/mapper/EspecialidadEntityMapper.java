package com.appointy.catalog.infrastructure.mapper;

import com.appointy.catalog.domain.model.Especialidad;
import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity.EspecialidadEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = Conversiones.class)
public interface EspecialidadEntityMapper {

    Especialidad aDominio(EspecialidadEntity entidad);

    EspecialidadEntity aEntidad(Especialidad dominio);
}

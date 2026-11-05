package com.appointy.catalog.infrastructure.mapper;

import com.appointy.catalog.domain.model.Bloqueo;
import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity.BloqueoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = Conversiones.class)
public interface BloqueoEntityMapper {

    Bloqueo aDominio(BloqueoEntity entidad);

    BloqueoEntity aEntidad(Bloqueo dominio);
}

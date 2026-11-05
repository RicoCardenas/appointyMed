package com.appointy.catalog.infrastructure.mapper;

import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity.CentroEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = Conversiones.class)
public interface CentroEntityMapper {

    Centro aDominio(CentroEntity entidad);

    CentroEntity aEntidad(Centro dominio);
}

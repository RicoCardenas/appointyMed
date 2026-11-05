package com.appointy.catalog.infrastructure.mapper;

import com.appointy.catalog.domain.model.HorarioAtencion;
import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity.HorarioAtencionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = Conversiones.class)
public interface HorarioAtencionEntityMapper {

    HorarioAtencion aDominio(HorarioAtencionEntity entidad);

    HorarioAtencionEntity aEntidad(HorarioAtencion dominio);
}

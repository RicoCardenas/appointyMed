package com.appointy.catalog.infrastructure.mapper;

import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity.ProfesionalEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = Conversiones.class)
public interface ProfesionalEntityMapper {

    Profesional aDominio(ProfesionalEntity entidad);

    ProfesionalEntity aEntidad(Profesional dominio);
}

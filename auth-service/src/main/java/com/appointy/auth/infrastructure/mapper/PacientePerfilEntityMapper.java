package com.appointy.auth.infrastructure.mapper;

import com.appointy.auth.domain.model.PacientePerfil;
import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.entity.PacientePerfilEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PacientePerfilEntityMapper {

    PacientePerfil aDominio(PacientePerfilEntity entidad);

    PacientePerfilEntity aEntidad(PacientePerfil perfil);
}

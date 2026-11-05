package com.appointy.auth.infrastructure.mapper;

import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.entity.UsuarioEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioEntityMapper {

    Usuario aDominio(UsuarioEntity entidad);

    UsuarioEntity aEntidad(Usuario usuario);
}

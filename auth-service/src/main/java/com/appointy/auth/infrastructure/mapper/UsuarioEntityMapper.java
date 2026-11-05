package com.appointy.auth.infrastructure.mapper;

import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.entity.UsuarioEntity;
import java.time.Instant;
import java.util.UUID;
import org.mapstruct.Mapper;

/** El dominio usa String y long; la entidad usa UUID e Instant. Las conversiones viven aqui, en el borde. */
@Mapper(componentModel = "spring")
public interface UsuarioEntityMapper {

    Usuario aDominio(UsuarioEntity entidad);

    UsuarioEntity aEntidad(Usuario usuario);

    default String idATexto(UUID id) {
        return id == null ? null : id.toString();
    }

    default UUID textoAId(String texto) {
        return texto == null ? null : UUID.fromString(texto);
    }

    default long instanteAMilisegundos(Instant instante) {
        return instante.toEpochMilli();
    }

    default Instant milisegundosAInstante(long milisegundos) {
        return Instant.ofEpochMilli(milisegundos);
    }
}

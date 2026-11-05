package com.appointy.auth.infrastructure.mapper;

import com.appointy.auth.domain.model.PacientePerfil;
import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.entity.PacientePerfilEntity;
import java.time.LocalDate;
import java.util.UUID;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PacientePerfilEntityMapper {

    PacientePerfil aDominio(PacientePerfilEntity entidad);

    PacientePerfilEntity aEntidad(PacientePerfil perfil);

    default String idATexto(UUID id) {
        return id == null ? null : id.toString();
    }

    default UUID textoAId(String texto) {
        return texto == null ? null : UUID.fromString(texto);
    }

    default String fechaATexto(LocalDate fecha) {
        return fecha == null ? null : fecha.toString();
    }

    default LocalDate textoAFecha(String texto) {
        return texto == null ? null : LocalDate.parse(texto);
    }
}

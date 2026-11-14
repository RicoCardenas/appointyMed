package com.appointy.auth.infrastructure.driven_adapters.jpa_repository.repository;

import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.entity.UsuarioEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpringDataUsuarioRepository extends JpaRepository<UsuarioEntity, UUID>, JpaSpecificationExecutor<UsuarioEntity> {

    Optional<UsuarioEntity> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByRol(Rol rol);

    long countByRol(Rol rol);

    long countByActivo(boolean activo);
}

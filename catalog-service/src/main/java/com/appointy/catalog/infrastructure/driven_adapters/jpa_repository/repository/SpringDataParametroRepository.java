package com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.repository;

import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity.ParametroServicioEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataParametroRepository extends JpaRepository<ParametroServicioEntity, String> {

    List<ParametroServicioEntity> findAllByOrderByClave();
}

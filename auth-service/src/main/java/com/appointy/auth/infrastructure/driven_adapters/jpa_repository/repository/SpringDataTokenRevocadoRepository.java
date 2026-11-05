package com.appointy.auth.infrastructure.driven_adapters.jpa_repository.repository;

import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.entity.TokenRevocadoEntity;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataTokenRevocadoRepository extends JpaRepository<TokenRevocadoEntity, String> {

    @Modifying
    @Query("delete from TokenRevocadoEntity t where t.expiraEn < :limite")
    int eliminarVencidosAntesDe(@Param("limite") Instant limite);
}

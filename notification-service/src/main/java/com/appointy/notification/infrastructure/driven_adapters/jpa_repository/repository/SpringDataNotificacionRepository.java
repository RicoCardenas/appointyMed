package com.appointy.notification.infrastructure.driven_adapters.jpa_repository.repository;

import com.appointy.notification.domain.model.EstadoNotificacion;
import com.appointy.notification.domain.model.TipoNotificacion;
import com.appointy.notification.infrastructure.driven_adapters.jpa_repository.entity.NotificacionEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpringDataNotificacionRepository extends JpaRepository<NotificacionEntity, UUID>, JpaSpecificationExecutor<NotificacionEntity> {

    Optional<NotificacionEntity> findByCitaIdAndTipoAndProgramadaPara(UUID citaId, TipoNotificacion tipo, Instant programadaPara);

    List<NotificacionEntity> findByTipoAndEstadoAndProgramadaParaLessThanEqualOrderByProgramadaParaAsc(
            TipoNotificacion tipo, EstadoNotificacion estado, Instant hasta, Pageable limite);

    List<NotificacionEntity> findByEstadoAndIntentosLessThanOrderByCreadoEnAsc(EstadoNotificacion estado, int maxIntentos, Pageable limite);

    List<NotificacionEntity> findByCitaIdAndTipoAndEstado(UUID citaId, TipoNotificacion tipo, EstadoNotificacion estado);

    List<NotificacionEntity> findByCitaIdOrderByCreadoEnAscTipoAscIdAsc(UUID citaId);

    long countByEstado(EstadoNotificacion estado);

    long countByTipo(TipoNotificacion tipo);
}

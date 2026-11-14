package com.appointy.notification.infrastructure.driven_adapters.jpa_repository;

import com.appointy.notification.domain.gateway.NotificacionRepositoryGateway;
import com.appointy.notification.domain.model.EstadoNotificacion;
import com.appointy.notification.domain.model.Estadisticas;
import com.appointy.notification.domain.model.FiltroNotificaciones;
import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.model.Pagina;
import com.appointy.notification.domain.model.TipoNotificacion;
import com.appointy.notification.infrastructure.driven_adapters.jpa_repository.entity.NotificacionEntity;
import com.appointy.notification.infrastructure.driven_adapters.jpa_repository.repository.SpringDataNotificacionRepository;
import com.appointy.notification.infrastructure.mapper.NotificacionEntityMapper;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class NotificacionJpaAdapter implements NotificacionRepositoryGateway {

    private final SpringDataNotificacionRepository repositorio;
    private final NotificacionEntityMapper mapper;

    public NotificacionJpaAdapter(SpringDataNotificacionRepository repositorio, NotificacionEntityMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Notificacion guardar(Notificacion notificacion) {
        return mapper.aDominio(repositorio.saveAndFlush(mapper.aEntidad(notificacion)));
    }

    @Override
    @Transactional(readOnly = true)
    public Notificacion buscarPorId(String id) {
        return repositorio.findById(UUID.fromString(id)).map(mapper::aDominio).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Notificacion buscarPorClave(String citaId, TipoNotificacion tipo, long programadaPara) {
        return repositorio.findByCitaIdAndTipoAndProgramadaPara(UUID.fromString(citaId), tipo, Instant.ofEpochMilli(programadaPara))
                .map(mapper::aDominio).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Notificacion[] recordatoriosVencidos(long ahora, int limite) {
        return aArreglo(repositorio.findByTipoAndEstadoAndProgramadaParaLessThanEqualOrderByProgramadaParaAsc(
                TipoNotificacion.RECORDATORIO, EstadoNotificacion.PENDIENTE, Instant.ofEpochMilli(ahora), PageRequest.of(0, limite)));
    }

    @Override
    @Transactional(readOnly = true)
    public Notificacion[] fallidasReintentables(int maxIntentos, int limite) {
        return aArreglo(repositorio.findByEstadoAndIntentosLessThanOrderByCreadoEnAsc(
                EstadoNotificacion.FALLIDA, maxIntentos, PageRequest.of(0, limite)));
    }

    @Override
    @Transactional
    public int cancelarRecordatoriosPendientes(String citaId) {
        List<NotificacionEntity> pendientes = repositorio.findByCitaIdAndTipoAndEstado(
                UUID.fromString(citaId), TipoNotificacion.RECORDATORIO, EstadoNotificacion.PENDIENTE);
        pendientes.forEach(p -> p.setEstado(EstadoNotificacion.CANCELADA));
        repositorio.saveAll(pendientes);
        return pendientes.size();
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina listar(FiltroNotificaciones filtro, int pagina, int tamano) {
        Specification<NotificacionEntity> spec = (raiz, consulta, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (filtro != null && filtro.estado() != null) condiciones.add(cb.equal(raiz.get("estado"), filtro.estado()));
            if (filtro != null && filtro.tipo() != null) condiciones.add(cb.equal(raiz.get("tipo"), filtro.tipo()));
            if (filtro != null && filtro.citaId() != null) condiciones.add(cb.equal(raiz.get("citaId"), UUID.fromString(filtro.citaId())));
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
        Page<NotificacionEntity> resultado = repositorio.findAll(spec, PageRequest.of(pagina, tamano, Sort.by(Sort.Order.desc("creadoEn"), Sort.Order.asc("id"))));
        return new Pagina(aArreglo(resultado.getContent()), pagina, tamano, resultado.getTotalElements(), resultado.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public Notificacion[] listarPorCita(String citaId) {
        return aArreglo(repositorio.findByCitaIdOrderByCreadoEnAscTipoAscIdAsc(UUID.fromString(citaId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Estadisticas estadisticas() {
        return new Estadisticas(
                repositorio.countByEstado(EstadoNotificacion.PENDIENTE), repositorio.countByEstado(EstadoNotificacion.ENVIADA),
                repositorio.countByEstado(EstadoNotificacion.FALLIDA), repositorio.countByEstado(EstadoNotificacion.CANCELADA),
                repositorio.countByTipo(TipoNotificacion.CONFIRMACION), repositorio.countByTipo(TipoNotificacion.REPROGRAMACION),
                repositorio.countByTipo(TipoNotificacion.CANCELACION), repositorio.countByTipo(TipoNotificacion.RECORDATORIO));
    }

    private Notificacion[] aArreglo(List<NotificacionEntity> entidades) {
        return entidades.stream().map(mapper::aDominio).toArray(Notificacion[]::new);
    }
}

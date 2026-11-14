package com.appointy.notification.infrastructure.driven_adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.notification.domain.model.EstadoNotificacion;
import com.appointy.notification.domain.model.Estadisticas;
import com.appointy.notification.domain.model.FiltroNotificaciones;
import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.model.Pagina;
import com.appointy.notification.domain.model.TipoNotificacion;
import com.appointy.notification.infrastructure.driven_adapters.jpa_repository.NotificacionJpaAdapter;
import com.appointy.notification.infrastructure.driven_adapters.jpa_repository.entity.NotificacionEntity;
import com.appointy.notification.infrastructure.driven_adapters.jpa_repository.repository.SpringDataNotificacionRepository;
import com.appointy.notification.infrastructure.mapper.NotificacionEntityMapper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@SuppressWarnings({"unchecked", "rawtypes"})
class NotificacionJpaAdapterTest {

    static final String ID = "11111111-1111-1111-1111-111111111111";
    static final String CITA = "22222222-2222-2222-2222-222222222222";

    final SpringDataNotificacionRepository repo = mock(SpringDataNotificacionRepository.class);
    final NotificacionEntityMapper mapper = new NotificacionEntityMapper();
    final NotificacionJpaAdapter adaptador = new NotificacionJpaAdapter(repo, mapper);

    static Notificacion dominio(long enviadaEn) {
        return new Notificacion(ID, CITA, TipoNotificacion.RECORDATORIO, "a@b.co", "asunto", "cuerpo", EstadoNotificacion.PENDIENTE, 2, "X", 1000, enviadaEn, 500);
    }

    @Test
    void elMapperConvierteIdaYVuelta() {
        for (long enviada : new long[] {0, 9000}) {
            Notificacion n = dominio(enviada);
            NotificacionEntity e = mapper.aEntidad(n);
            assertEquals(UUID.fromString(ID), e.getId());
            assertEquals(enviada == 0, e.getEnviadaEn() == null);
            assertEquals(n, mapper.aDominio(e));
        }
    }

    @Test
    void guardaYBusca() {
        NotificacionEntity e = mapper.aEntidad(dominio(0));
        when(repo.saveAndFlush(any())).thenReturn(e);
        assertEquals(ID, adaptador.guardar(dominio(0)).id());
        when(repo.findById(UUID.fromString(ID))).thenReturn(Optional.of(e));
        assertEquals(CITA, adaptador.buscarPorId(ID).citaId());
        when(repo.findById(UUID.fromString(CITA))).thenReturn(Optional.empty());
        assertNull(adaptador.buscarPorId(CITA));
        when(repo.findByCitaIdAndTipoAndProgramadaPara(UUID.fromString(CITA), TipoNotificacion.RECORDATORIO, Instant.ofEpochMilli(1000))).thenReturn(Optional.of(e));
        assertEquals(ID, adaptador.buscarPorClave(CITA, TipoNotificacion.RECORDATORIO, 1000).id());
        assertNull(adaptador.buscarPorClave(CITA, TipoNotificacion.CANCELACION, 1000));
    }

    @Test
    void consultasDeLosJobs() {
        NotificacionEntity e = mapper.aEntidad(dominio(0));
        when(repo.findByTipoAndEstadoAndProgramadaParaLessThanEqualOrderByProgramadaParaAsc(eq(TipoNotificacion.RECORDATORIO), eq(EstadoNotificacion.PENDIENTE), any(), any(Pageable.class)))
                .thenReturn(List.of(e));
        when(repo.findByEstadoAndIntentosLessThanOrderByCreadoEnAsc(eq(EstadoNotificacion.FALLIDA), eq(3), any(Pageable.class))).thenReturn(List.of(e, e));
        assertEquals(1, adaptador.recordatoriosVencidos(5000, 10).length);
        assertEquals(2, adaptador.fallidasReintentables(3, 10).length);
        when(repo.findByCitaIdOrderByCreadoEnAscTipoAscIdAsc(UUID.fromString(CITA))).thenReturn(List.of(e));
        assertEquals(1, adaptador.listarPorCita(CITA).length);
    }

    @Test
    void cancelaLosRecordatoriosPendientes() {
        NotificacionEntity e = mapper.aEntidad(dominio(0));
        when(repo.findByCitaIdAndTipoAndEstado(UUID.fromString(CITA), TipoNotificacion.RECORDATORIO, EstadoNotificacion.PENDIENTE)).thenReturn(List.of(e));
        assertEquals(1, adaptador.cancelarRecordatoriosPendientes(CITA));
        assertEquals(EstadoNotificacion.CANCELADA, e.getEstado());
        verify(repo).saveAll(List.of(e));
    }

    @Test
    void listaPaginadaYConstruyeLaEspecificacion() {
        NotificacionEntity e = mapper.aEntidad(dominio(0));
        when(repo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(e), PageRequest.of(1, 5), 6));
        Pagina p = adaptador.listar(new FiltroNotificaciones(EstadoNotificacion.FALLIDA, TipoNotificacion.RECORDATORIO, CITA), 1, 5);
        assertEquals(6, p.totalElementos());
        assertEquals(2, p.totalPaginas());
        ArgumentCaptor<Specification> spec = ArgumentCaptor.forClass(Specification.class);
        verify(repo).findAll(spec.capture(), any(Pageable.class));
        // se ejecuta la especificacion para comprobar que arma un predicado por cada filtro sin lanzar errores
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        spec.getValue().toPredicate(mock(Root.class), mock(CriteriaQuery.class), cb);
        verify(cb, org.mockito.Mockito.times(3)).equal(any(), any(Object.class));
        adaptador.listar(null, 0, 5);
    }

    @Test
    void estadisticasCuentaPorEstadoYTipo() {
        when(repo.countByEstado(EstadoNotificacion.PENDIENTE)).thenReturn(1L);
        when(repo.countByEstado(EstadoNotificacion.ENVIADA)).thenReturn(2L);
        when(repo.countByEstado(EstadoNotificacion.FALLIDA)).thenReturn(3L);
        when(repo.countByEstado(EstadoNotificacion.CANCELADA)).thenReturn(4L);
        when(repo.countByTipo(TipoNotificacion.RECORDATORIO)).thenReturn(8L);
        Estadisticas s = adaptador.estadisticas();
        assertEquals(new Estadisticas(1, 2, 3, 4, 0, 0, 0, 8), s);
    }
}

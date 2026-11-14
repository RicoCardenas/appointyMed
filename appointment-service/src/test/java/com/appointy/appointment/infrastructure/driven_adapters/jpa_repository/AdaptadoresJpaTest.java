package com.appointy.appointment.infrastructure.driven_adapters.jpa_repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.EstadisticasCitas;
import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.domain.model.EstadoFranja;
import com.appointy.appointment.domain.model.FiltroCitas;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.model.HistorialCita;
import com.appointy.appointment.domain.model.PaginaCitas;
import com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.entity.CitaEntity;
import com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.entity.FranjaEntity;
import com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.entity.HistorialCitaEntity;
import com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.repository.SpringDataBloqueoPgRepository;
import com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.repository.SpringDataCitaRepository;
import com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.repository.SpringDataFranjaRepository;
import com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.repository.SpringDataHistorialCitaRepository;
import com.appointy.appointment.infrastructure.mapper.CitaEntityMapper;
import com.appointy.appointment.infrastructure.mapper.Conversiones;
import com.appointy.appointment.infrastructure.mapper.FranjaEntityMapper;
import com.appointy.appointment.infrastructure.mapper.HistorialCitaEntityMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings({"unchecked", "rawtypes"})
class AdaptadoresJpaTest {

    static final String ID = "11111111-1111-1111-1111-111111111111";
    static final String P = "22222222-2222-2222-2222-222222222222";
    static final String PROF = "33333333-3333-3333-3333-333333333333";
    static final String F = "44444444-4444-4444-4444-444444444444";

    final Conversiones conv = new Conversiones();
    final CitaEntityMapper citaMapper = new CitaEntityMapper(conv);
    final FranjaEntityMapper franjaMapper = new FranjaEntityMapper(conv);
    final HistorialCitaEntityMapper historialMapper = new HistorialCitaEntityMapper(conv);
    final SpringDataCitaRepository citaRepo = mock(SpringDataCitaRepository.class);
    final SpringDataFranjaRepository franjaRepo = mock(SpringDataFranjaRepository.class);
    final SpringDataBloqueoPgRepository bloqueoRepo = mock(SpringDataBloqueoPgRepository.class);
    final BloqueosPostgres bloqueos = new BloqueosPostgres(bloqueoRepo);

    static Cita cita() {
        return new Cita(ID, P, PROF, ID, ID, F, EstadoCita.CONFIRMADA, 1000, 2000, 3L);
    }

    static Franja franja() {
        return new Franja(F, PROF, ID, "2026-11-20", 480, 510, EstadoFranja.DISPONIBLE, 2L);
    }

    @Test
    void conversionesYMappersIdaYVuelta() {
        assertNull(conv.idATexto(null));
        assertNull(conv.textoAId(null));
        assertEquals(510, conv.horaAMinutos(LocalTime.of(8, 30)));
        assertEquals(LocalTime.of(8, 30), conv.minutosAHora(510));
        assertEquals("2026-11-20", conv.fechaATexto(LocalDate.of(2026, 11, 20)));
        assertEquals(LocalDate.of(2026, 11, 20), conv.textoAFecha("2026-11-20"));
        assertEquals(77, conv.instanteAMilisegundos(conv.milisegundosAInstante(77)));
        assertEquals(cita(), citaMapper.aDominio(citaMapper.aEntidad(cita())));
        assertEquals(franja(), franjaMapper.aDominio(franjaMapper.aEntidad(franja())));
        HistorialCita h = new HistorialCita(ID, ID, null, EstadoCita.CONFIRMADA, P, "PACIENTE", null, F, "Reserva", 5000);
        assertEquals(h, historialMapper.aDominio(historialMapper.aEntidad(h)));
        HistorialCita h2 = new HistorialCita(ID, ID, EstadoCita.CONFIRMADA, EstadoCita.CANCELADA, P, "PACIENTE", F, null, "Cancelada", 6000);
        assertEquals(h2, historialMapper.aDominio(historialMapper.aEntidad(h2)));
    }

    @Test
    void citasGuardaBuscaYListaPorPeriodo() {
        CitaJpaAdapter a = new CitaJpaAdapter(citaRepo, citaMapper, bloqueos);
        CitaEntity e = citaMapper.aEntidad(cita());
        when(citaRepo.saveAndFlush(any())).thenReturn(e);
        assertEquals(ID, a.guardar(cita()).id());
        when(citaRepo.findById(UUID.fromString(ID))).thenReturn(Optional.of(e));
        assertEquals(P, a.buscarPorId(ID).pacienteId());
        assertNull(a.buscarPorId(F));
        when(citaRepo.listarDePaciente(any(), any(), any())).thenReturn(List.of(e));
        assertEquals(1, a.listarDePaciente(P, "2026-01-01", "2026-12-31").length);
        when(citaRepo.listarDeProfesional(any(), eq(EstadoCita.CONFIRMADA), any(), any())).thenReturn(List.of(e, e));
        assertEquals(2, a.listarAgendaConfirmada(PROF, "2026-01-01", "2026-12-31").length);
        when(citaRepo.idsEnRango(any(), any(), eq(EstadoCita.CONFIRMADA), any(), any())).thenReturn(List.of(UUID.fromString(ID)));
        assertEquals(ID, a.idsConfirmadasEnRango(PROF, ID, "2026-01-01", "2026-12-31")[0]);
        when(citaRepo.existsByProfesionalIdAndPacienteIdAndEstadoIn(any(), any(), anyCollection())).thenReturn(true);
        assertTrue(a.existeRelacion(PROF, P));
    }

    @Test
    void citasBloqueaAlPacienteConEsperaMaxima() {
        new CitaJpaAdapter(citaRepo, citaMapper, bloqueos).bloquearPaciente(P);
        verify(bloqueoRepo).fijarEsperaMaxima("5000");
        verify(bloqueoRepo).bloquearClave("paciente:" + P);
    }

    @Test
    void citasListaPaginadaConEspecificacionYEstadisticas() {
        CitaJpaAdapter a = new CitaJpaAdapter(citaRepo, citaMapper, bloqueos);
        when(citaRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(citaMapper.aEntidad(cita())), PageRequest.of(1, 5), 6));
        PaginaCitas p = a.listar(new FiltroCitas(EstadoCita.CANCELADA, PROF, P), 1, 5);
        assertEquals(6, p.totalElementos());
        assertEquals(2, p.totalPaginas());
        ArgumentCaptor<Specification> spec = ArgumentCaptor.forClass(Specification.class);
        verify(citaRepo).findAll(spec.capture(), any(Pageable.class));
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        spec.getValue().toPredicate(mock(Root.class), mock(CriteriaQuery.class), cb);
        verify(cb, org.mockito.Mockito.times(3)).equal(any(), any(Object.class));
        a.listar(null, 0, 5);
        when(citaRepo.countByEstado(EstadoCita.CONFIRMADA)).thenReturn(1L);
        when(citaRepo.countByEstado(EstadoCita.NO_ASISTIDA)).thenReturn(4L);
        assertEquals(new EstadisticasCitas(1, 0, 0, 4), a.estadisticas());
    }

    @Test
    void franjasConsultasYBloqueos() {
        FranjaJpaAdapter a = new FranjaJpaAdapter(franjaRepo, franjaMapper, conv, bloqueos);
        FranjaEntity e = franjaMapper.aEntidad(franja());
        when(franjaRepo.findById(UUID.fromString(F))).thenReturn(Optional.of(e));
        assertEquals(F, a.buscarPorId(F).id());
        assertNull(a.buscarPorId(ID));
        when(franjaRepo.buscarParaActualizar(UUID.fromString(F))).thenReturn(Optional.of(e));
        assertEquals(F, a.buscarParaActualizar(F).id());
        assertNull(a.buscarParaActualizar(ID));
        when(franjaRepo.bloquearRango(any(), any(), any(), any())).thenReturn(List.of(e));
        assertEquals(1, a.bloquearRango(PROF, ID, "2026-11-20", "2026-11-21").length);
        when(franjaRepo.findByIdIn(anyCollection())).thenReturn(List.of(e));
        assertEquals(1, a.buscarPorIds(new String[] {F}).length);
        assertEquals(0, a.buscarPorIds(new String[0]).length);
        verify(franjaRepo, never()).findByIdIn(List.of());
        when(franjaRepo.findByProfesionalIdAndCentroIdAndFechaAndEstadoOrderByHoraInicio(any(), any(), any(), eq(EstadoFranja.DISPONIBLE))).thenReturn(List.of(e));
        assertEquals(1, a.listarDisponibles(PROF, ID, "2026-11-20").length);
        when(franjaRepo.franjasDeCitasDelPaciente(any(), any(), eq(EstadoCita.CONFIRMADA))).thenReturn(List.of(e));
        assertEquals(1, a.franjasDeCitasConfirmadasDelPaciente(P, "2026-11-20").length);
    }

    @Test
    void franjasGuardaEInsertaSiNoExiste() {
        FranjaJpaAdapter a = new FranjaJpaAdapter(franjaRepo, franjaMapper, conv, bloqueos);
        when(franjaRepo.saveAndFlush(any())).thenReturn(franjaMapper.aEntidad(franja()));
        assertEquals(F, a.guardar(franja()).id());
        when(franjaRepo.insertarSiNoExiste(any(), any(), any(), any(), any(), any())).thenReturn(1, 0);
        assertTrue(a.insertarSiNoExiste(franja()));
        assertEquals(false, a.insertarSiNoExiste(franja()));
    }

    @Test
    void historialSoloInsertaYLista() {
        SpringDataHistorialCitaRepository repo = mock(SpringDataHistorialCitaRepository.class);
        EntityManager em = mock(EntityManager.class);
        HistorialCitaJpaAdapter a = new HistorialCitaJpaAdapter(repo, historialMapper);
        ReflectionTestUtils.setField(a, "em", em);
        HistorialCita h = new HistorialCita(ID, ID, null, EstadoCita.CONFIRMADA, P, "PACIENTE", null, F, "Reserva", 5000);
        a.registrar(h);
        verify(em).persist(any(HistorialCitaEntity.class));
        when(repo.findByCitaIdOrderByFechaAsc(UUID.fromString(ID))).thenReturn(List.of(historialMapper.aEntidad(h)));
        assertEquals(1, a.listarPorCita(ID).length);
    }
}

package com.appointy.clinicalhistory.infrastructure.driven_adapters.jpa_repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.clinicalhistory.domain.model.AccionAcceso;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;
import com.appointy.clinicalhistory.domain.model.LogAcceso;
import com.appointy.clinicalhistory.domain.model.PaginaEntradas;
import com.appointy.clinicalhistory.domain.model.ResultadoAcceso;
import com.appointy.clinicalhistory.infrastructure.driven_adapters.jpa_repository.EntradaClinicaJpaAdapter;
import com.appointy.clinicalhistory.infrastructure.driven_adapters.jpa_repository.LogAccesoJpaAdapter;
import com.appointy.clinicalhistory.infrastructure.driven_adapters.jpa_repository.entity.EntradaClinicaEntity;
import com.appointy.clinicalhistory.infrastructure.driven_adapters.jpa_repository.entity.LogAccesoEntity;
import com.appointy.clinicalhistory.infrastructure.driven_adapters.jpa_repository.repository.SpringDataEntradaClinicaRepository;
import com.appointy.clinicalhistory.infrastructure.mapper.ClinicaEntityMapper;
import com.appointy.clinicalhistory.infrastructure.mapper.Conversiones;
import jakarta.persistence.EntityManager;
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
import org.springframework.test.util.ReflectionTestUtils;

class AdaptadoresJpaTest {

    static final String ID = "11111111-1111-1111-1111-111111111111";
    static final String P = "22222222-2222-2222-2222-222222222222";
    static final String PROF = "33333333-3333-3333-3333-333333333333";
    static final String CITA = "44444444-4444-4444-4444-444444444444";

    final Conversiones conv = new Conversiones();
    final ClinicaEntityMapper mapper = new ClinicaEntityMapper(conv);
    final SpringDataEntradaClinicaRepository repo = mock(SpringDataEntradaClinicaRepository.class);
    final EntityManager em = mock(EntityManager.class);

    static EntradaClinica entrada(String corrige) {
        return new EntradaClinica(ID, P, PROF, CITA, "motivo", "dx", "notas", "ind", corrige, 5000);
    }

    @Test
    void conversionesSimples() {
        assertNull(conv.idATexto(null));
        assertNull(conv.textoAId(null));
        assertNull(conv.fechaATexto(null));
        assertNull(conv.textoAFecha(null));
        assertEquals(ID, conv.idATexto(UUID.fromString(ID)));
        assertEquals(UUID.fromString(ID), conv.textoAId(ID));
        assertEquals(510, conv.horaAMinutos(LocalTime.of(8, 30)));
        assertEquals(LocalTime.of(8, 30), conv.minutosAHora(510));
        assertEquals("2026-11-14", conv.fechaATexto(LocalDate.of(2026, 11, 14)));
        assertEquals(LocalDate.of(2026, 11, 14), conv.textoAFecha("2026-11-14"));
        assertEquals(1234, conv.instanteAMilisegundos(conv.milisegundosAInstante(1234)));
    }

    @Test
    void elMapperConvierteEntradasYLogs() {
        for (String corrige : new String[] {null, "55555555-5555-5555-5555-555555555555"}) {
            EntradaClinica d = entrada(corrige);
            assertEquals(d, mapper.aDominio(mapper.aEntidad(d)));
        }
        LogAccesoEntity e = mapper.aEntidad(new LogAcceso(ID, P, "PROFESIONAL", null, AccionAcceso.LECTURA, null, 7000, ResultadoAcceso.DENEGADO));
        assertEquals(ResultadoAcceso.DENEGADO, e.getResultado());
        assertNull(e.getPacienteId());
        assertEquals(7000, e.getFecha().toEpochMilli());
    }

    @Test
    void elAdaptadorDeEntradasInsertaYConsulta() {
        EntradaClinicaJpaAdapter adaptador = new EntradaClinicaJpaAdapter(repo, mapper);
        ReflectionTestUtils.setField(adaptador, "em", em);
        adaptador.registrar(entrada(null));
        verify(em).persist(any(EntradaClinicaEntity.class));
        verify(em).flush();

        EntradaClinicaEntity e = mapper.aEntidad(entrada(null));
        when(repo.findById(UUID.fromString(ID))).thenReturn(Optional.of(e));
        assertEquals(ID, adaptador.buscarPorId(ID).id());
        assertNull(adaptador.buscarPorId(CITA));
        when(repo.findByPacienteIdOrderByCreadoEnAscIdAsc(UUID.fromString(P))).thenReturn(List.of(e, e));
        assertEquals(2, adaptador.listarDePaciente(P).length);
        when(repo.existsByCitaIdAndCorrigeAIdIsNull(UUID.fromString(CITA))).thenReturn(true);
        assertEquals(true, adaptador.existeOriginalDeCita(CITA));
    }

    @Test
    void listaLasEntradasDeUnProfesionalPaginadas() {
        EntradaClinicaJpaAdapter adaptador = new EntradaClinicaJpaAdapter(repo, mapper);
        EntradaClinicaEntity e = mapper.aEntidad(entrada(null));
        when(repo.findByProfesionalId(any(UUID.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(e), PageRequest.of(1, 5), 6));
        PaginaEntradas p = adaptador.listarDeProfesional(PROF, 1, 5);
        assertEquals(6, p.totalElementos());
        assertEquals(2, p.totalPaginas());
        assertEquals(1, p.contenido().length);
    }

    @Test
    void elAdaptadorDeLogSoloInserta() {
        LogAccesoJpaAdapter adaptador = new LogAccesoJpaAdapter(mapper);
        ReflectionTestUtils.setField(adaptador, "em", em);
        adaptador.registrar(new LogAcceso(ID, P, "PACIENTE", P, AccionAcceso.LECTURA, null, 1, ResultadoAcceso.PERMITIDO));
        ArgumentCaptor<LogAccesoEntity> captura = ArgumentCaptor.forClass(LogAccesoEntity.class);
        verify(em).persist(captura.capture());
        assertEquals(AccionAcceso.LECTURA, captura.getValue().getAccion());
    }
}

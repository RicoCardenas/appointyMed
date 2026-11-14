package com.appointy.catalog.infrastructure.driven_adapters.jpa_repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.catalog.domain.model.Bloqueo;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.model.Especialidad;
import com.appointy.catalog.domain.model.FiltroProfesionales;
import com.appointy.catalog.domain.model.HorarioAtencion;
import com.appointy.catalog.domain.model.PaginaProfesionales;
import com.appointy.catalog.domain.model.ParametroServicio;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity.CentroEntity;
import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity.EspecialidadEntity;
import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity.ProfesionalEntity;
import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.repository.*;
import com.appointy.catalog.infrastructure.mapper.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.lang.reflect.Field;
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

@SuppressWarnings({"unchecked", "rawtypes"})
class AdaptadoresJpaTest {

    static final String U = "11111111-1111-1111-1111-111111111111";
    static final String V = "22222222-2222-2222-2222-222222222222";

    static final Conversiones CONV = new Conversiones();

    /** Los mappers de MapStruct reciben Conversiones por campo; fuera de Spring se asigna a mano. */
    static <T> T mapper(Class<T> tipo) throws Exception {
        T m = tipo.getDeclaredConstructor().newInstance();
        for (Field f : tipo.getDeclaredFields()) {
            if (f.getType() == Conversiones.class) {
                f.setAccessible(true);
                f.set(m, CONV);
            }
        }
        return m;
    }

    @Test
    void conversiones() {
        assertNull(CONV.idATexto(null));
        assertNull(CONV.textoAId(null));
        assertEquals(U, CONV.idATexto(UUID.fromString(U)));
        assertEquals(510, CONV.horaAMinutos(LocalTime.of(8, 30)));
        assertEquals(LocalTime.of(8, 30), CONV.minutosAHora(510));
        assertEquals("2026-11-20", CONV.fechaATexto(LocalDate.of(2026, 11, 20)));
        assertEquals(LocalDate.of(2026, 11, 20), CONV.textoAFecha("2026-11-20"));
        assertEquals(5, CONV.instanteAMilisegundos(CONV.milisegundosAInstante(5)));
    }

    @Test
    void centrosEspecialidadesParametrosHorariosYBloqueos() throws Exception {
        CentroEntityMapperImpl cm = mapper(CentroEntityMapperImpl.class);
        SpringDataCentroRepository cr = mock(SpringDataCentroRepository.class);
        CentroJpaAdapter centros = new CentroJpaAdapter(cr, cm);
        Centro centro = new Centro(U, "Sede", "Calle 1", "Bogota", "300", true);
        CentroEntity ce = cm.aEntidad(centro);
        assertEquals(centro, cm.aDominio(ce));
        when(cr.save(any())).thenReturn(ce);
        assertEquals(U, centros.guardar(centro).id());
        when(cr.findById(UUID.fromString(U))).thenReturn(Optional.of(ce));
        assertEquals("Sede", centros.buscarPorId(U).nombre());
        assertNull(centros.buscarPorId(V));
        when(cr.listarActivosDeProfesional(UUID.fromString(V))).thenReturn(List.of(ce));
        assertEquals(1, centros.listarActivosDeProfesional(V).length);

        EspecialidadEntityMapperImpl em = mapper(EspecialidadEntityMapperImpl.class);
        SpringDataEspecialidadRepository er = mock(SpringDataEspecialidadRepository.class);
        EspecialidadJpaAdapter especialidades = new EspecialidadJpaAdapter(er, em);
        Especialidad esp = new Especialidad(U, "Cardiologia", "d", true);
        EspecialidadEntity ee = em.aEntidad(esp);
        assertEquals(esp, em.aDominio(ee));
        when(er.save(any())).thenReturn(ee);
        assertEquals(U, especialidades.guardar(esp).id());
        when(er.findById(UUID.fromString(U))).thenReturn(Optional.of(ee));
        assertEquals("Cardiologia", especialidades.buscarPorId(U).nombre());
        assertNull(especialidades.buscarPorId(V));
        when(er.findByNombre("Cardiologia")).thenReturn(ee);
        assertEquals(U, especialidades.buscarPorNombre("Cardiologia").id());
        assertNull(especialidades.buscarPorNombre("Otra"));
        when(er.findAllByOrderByNombre()).thenReturn(List.of(ee, ee));
        when(er.findByActivoTrueOrderByNombre()).thenReturn(List.of(ee));
        assertEquals(2, especialidades.listar(true).length);
        assertEquals(1, especialidades.listar(false).length);

        ParametroServicioEntityMapperImpl pm = mapper(ParametroServicioEntityMapperImpl.class);
        SpringDataParametroRepository pr = mock(SpringDataParametroRepository.class);
        ParametroJpaAdapter parametros = new ParametroJpaAdapter(pr, pm);
        ParametroServicio p = new ParametroServicio("k", "v", "d");
        assertEquals(p, pm.aDominio(pm.aEntidad(p)));
        when(pr.save(any())).thenReturn(pm.aEntidad(p));
        assertEquals("v", parametros.guardar(p).valor());
        when(pr.findById("k")).thenReturn(Optional.of(pm.aEntidad(p)));
        assertEquals("d", parametros.buscarPorClave("k").descripcion());
        assertNull(parametros.buscarPorClave("x"));
        when(pr.findAllByOrderByClave()).thenReturn(List.of(pm.aEntidad(p)));
        assertEquals(1, parametros.listar().length);

        HorarioAtencionEntityMapperImpl hm = mapper(HorarioAtencionEntityMapperImpl.class);
        SpringDataHorarioRepository hr = mock(SpringDataHorarioRepository.class);
        HorarioJpaAdapter horarios = new HorarioJpaAdapter(hr, hm);
        HorarioAtencion h = new HorarioAtencion(U, V, U, 1, 480, 540, 30, true);
        assertEquals(h, hm.aDominio(hm.aEntidad(h)));
        when(hr.save(any())).thenReturn(hm.aEntidad(h));
        assertEquals(U, horarios.guardar(h).id());
        when(hr.findById(UUID.fromString(U))).thenReturn(Optional.of(hm.aEntidad(h)));
        assertEquals(480, horarios.buscarPorId(U).horaInicio());
        assertNull(horarios.buscarPorId(V));
        when(hr.findByProfesionalIdOrderByDiaSemanaAscHoraInicioAsc(UUID.fromString(V))).thenReturn(List.of(hm.aEntidad(h)));
        assertEquals(1, horarios.listarDeProfesional(V).length);
        when(hr.countByProfesionalIdAndCentroId(UUID.fromString(V), UUID.fromString(U))).thenReturn(3);
        assertEquals(3, horarios.contarPorProfesionalYCentro(V, U));

        BloqueoEntityMapperImpl bm = mapper(BloqueoEntityMapperImpl.class);
        SpringDataBloqueoRepository br = mock(SpringDataBloqueoRepository.class);
        BloqueoJpaAdapter bloqueos = new BloqueoJpaAdapter(br, bm);
        Bloqueo b = new Bloqueo(U, V, U, "2026-11-20", "2026-11-21", "Congreso", U, 1000);
        assertEquals(b, bm.aDominio(bm.aEntidad(b)));
        when(br.save(any())).thenReturn(bm.aEntidad(b));
        assertEquals("Congreso", bloqueos.guardar(b).motivo());
        when(br.countByProfesionalIdAndCentroId(UUID.fromString(V), UUID.fromString(U))).thenReturn(2);
        assertEquals(2, bloqueos.contarPorProfesionalYCentro(V, U));
    }

    @Test
    void profesionales() throws Exception {
        ProfesionalEntityMapperImpl pm = mapper(ProfesionalEntityMapperImpl.class);
        SpringDataProfesionalRepository repo = mock(SpringDataProfesionalRepository.class);
        ProfesionalJpaAdapter a = new ProfesionalJpaAdapter(repo, pm);
        Profesional p = new Profesional(U, V, "Ana", "Perez", "RM-1", true);
        ProfesionalEntity e = pm.aEntidad(p);
        assertEquals(p, pm.aDominio(e));
        when(repo.save(any())).thenReturn(e);
        assertEquals(U, a.guardar(p).id());
        when(repo.findById(UUID.fromString(U))).thenReturn(Optional.of(e));
        assertEquals("Ana", a.buscarPorId(U).nombres());
        assertNull(a.buscarPorId(V));
        when(repo.findByUsuarioId(UUID.fromString(V))).thenReturn(e);
        assertEquals(U, a.buscarPorUsuarioId(V).id());
        assertNull(a.buscarPorUsuarioId(U));
        when(repo.findByRegistroMedico("RM-1")).thenReturn(e);
        assertEquals(U, a.buscarPorRegistroMedico("RM-1").id());
        assertNull(a.buscarPorRegistroMedico("X"));
        when(repo.listarActivosPorEspecialidad(UUID.fromString(V))).thenReturn(List.of(e));
        assertEquals(1, a.listarActivosPorEspecialidad(V).length);
        when(repo.idsEspecialidades(UUID.fromString(U))).thenReturn(List.of(UUID.fromString(V), UUID.fromString(U)));
        assertEquals(U, a.idsEspecialidades(U)[0]);
        when(repo.idsCentros(UUID.fromString(U))).thenReturn(List.of(UUID.fromString(V)));
        assertEquals(V, a.idsCentros(U)[0]);
        when(repo.atiendeEnCentro(UUID.fromString(U), UUID.fromString(V))).thenReturn(true);
        assertTrue(a.atiendeEnCentro(U, V));
        assertFalse(a.tieneEspecialidad(U, V));
    }

    @Test
    void profesionalesReemplazaAsociaciones() throws Exception {
        SpringDataProfesionalRepository repo = mock(SpringDataProfesionalRepository.class);
        ProfesionalJpaAdapter a = new ProfesionalJpaAdapter(repo, mapper(ProfesionalEntityMapperImpl.class));
        a.reemplazarEspecialidades(U, new String[] {V});
        a.reemplazarCentros(U, new String[] {V, U});
        var orden = inOrder(repo);
        orden.verify(repo).borrarEspecialidades(UUID.fromString(U));
        orden.verify(repo).insertarEspecialidad(UUID.fromString(U), UUID.fromString(V));
        orden.verify(repo).borrarCentros(UUID.fromString(U));
        verify(repo).insertarCentro(UUID.fromString(U), UUID.fromString(U));
    }

    @Test
    void profesionalesListaPaginadaConFiltros() throws Exception {
        ProfesionalEntityMapperImpl pm = mapper(ProfesionalEntityMapperImpl.class);
        SpringDataProfesionalRepository repo = mock(SpringDataProfesionalRepository.class);
        ProfesionalJpaAdapter a = new ProfesionalJpaAdapter(repo, pm);
        when(repo.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(pm.aEntidad(new Profesional(U, V, "Ana", "Perez", "RM-1", true))), PageRequest.of(1, 5), 6));
        PaginaProfesionales p = a.listar(new FiltroProfesionales("an_a%", true), 1, 5);
        assertEquals(6, p.totalElementos());
        assertEquals(2, p.totalPaginas());
        ArgumentCaptor<Specification> spec = ArgumentCaptor.forClass(Specification.class);
        verify(repo).findAll(spec.capture(), any(Pageable.class));
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        spec.getValue().toPredicate(mock(Root.class), mock(CriteriaQuery.class), cb);
        verify(cb).equal(any(), any(Object.class));
        verify(cb, org.mockito.Mockito.times(3)).like(org.mockito.ArgumentMatchers.<jakarta.persistence.criteria.Expression<String>>any(), any(String.class), org.mockito.ArgumentMatchers.anyChar());
        a.listar(null, 0, 5);
        a.listar(new FiltroProfesionales("  ", null), 0, 5);
    }
}

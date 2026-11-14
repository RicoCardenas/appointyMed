package com.appointy.auth.infrastructure.driven_adapters.jpa_repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.auth.domain.model.EstadisticasUsuarios;
import com.appointy.auth.domain.model.FiltroUsuarios;
import com.appointy.auth.domain.model.PacientePerfil;
import com.appointy.auth.domain.model.PaginaUsuarios;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.TokenRevocado;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.entity.PacientePerfilEntity;
import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.entity.TokenRevocadoEntity;
import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.entity.UsuarioEntity;
import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.repository.SpringDataPacientePerfilRepository;
import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.repository.SpringDataTokenRevocadoRepository;
import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.repository.SpringDataUsuarioRepository;
import com.appointy.auth.infrastructure.mapper.PacientePerfilEntityMapperImpl;
import com.appointy.auth.infrastructure.mapper.UsuarioEntityMapperImpl;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
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

    static final String ID = "11111111-1111-1111-1111-111111111111";

    final SpringDataUsuarioRepository repo = mock(SpringDataUsuarioRepository.class);
    final UsuarioEntityMapperImpl mapper = new UsuarioEntityMapperImpl();
    final UsuarioJpaAdapter adaptador = new UsuarioJpaAdapter(repo, mapper);

    static Usuario usuario() {
        return new Usuario(ID, "ana@correo.co", "hash", Rol.PACIENTE, true, 1000, 2000, 3L);
    }

    @Test
    void usuariosGuardaYBusca() {
        UsuarioEntity e = mapper.aEntidad(usuario());
        assertEquals(usuario(), mapper.aDominio(e));
        when(repo.save(any())).thenReturn(e);
        assertEquals(ID, adaptador.guardar(usuario()).id());
        when(repo.findById(UUID.fromString(ID))).thenReturn(Optional.of(e));
        assertEquals("ana@correo.co", adaptador.buscarPorId(ID).correo());
        assertNull(adaptador.buscarPorId("22222222-2222-2222-2222-222222222222"));
        when(repo.findByCorreo("ana@correo.co")).thenReturn(Optional.of(e));
        assertEquals(ID, adaptador.buscarPorCorreo("ana@correo.co").id());
        assertNull(adaptador.buscarPorCorreo("otro@correo.co"));
        when(repo.existsByCorreo("ana@correo.co")).thenReturn(true);
        assertTrue(adaptador.existePorCorreo("ana@correo.co"));
        when(repo.existsByRol(Rol.ADMINISTRATIVO)).thenReturn(true);
        assertTrue(adaptador.existeAlgunoConRol(Rol.ADMINISTRATIVO));
    }

    @Test
    void usuariosListaPaginadaConEspecificacion() {
        when(repo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(mapper.aEntidad(usuario())), PageRequest.of(1, 5), 6));
        PaginaUsuarios p = adaptador.listar(new FiltroUsuarios(Rol.PACIENTE, true), 1, 5);
        assertEquals(6, p.totalElementos());
        assertEquals(2, p.totalPaginas());
        ArgumentCaptor<Specification> spec = ArgumentCaptor.forClass(Specification.class);
        verify(repo).findAll(spec.capture(), any(Pageable.class));
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        spec.getValue().toPredicate(mock(Root.class), mock(CriteriaQuery.class), cb);
        verify(cb, org.mockito.Mockito.times(2)).equal(any(), any(Object.class));
        adaptador.listar(null, 0, 5);
    }

    @Test
    void usuariosEstadisticas() {
        when(repo.countByRol(Rol.PACIENTE)).thenReturn(5L);
        when(repo.countByRol(Rol.PROFESIONAL)).thenReturn(2L);
        when(repo.countByActivo(true)).thenReturn(7L);
        when(repo.countByActivo(false)).thenReturn(1L);
        assertEquals(new EstadisticasUsuarios(5, 2, 0, 7, 1), adaptador.estadisticas());
    }

    @Test
    void perfilesGuardaYBusca() {
        SpringDataPacientePerfilRepository perfiles = mock(SpringDataPacientePerfilRepository.class);
        PacientePerfilEntityMapperImpl pm = new PacientePerfilEntityMapperImpl();
        PacientePerfilJpaAdapter a = new PacientePerfilJpaAdapter(perfiles, pm);
        PacientePerfil d = new PacientePerfil(ID, "12345", "Ana", "Perez", "1990-05-01", "3001234567", "Calle 1", "Bogota");
        PacientePerfilEntity e = pm.aEntidad(d);
        assertEquals(d, pm.aDominio(e));
        when(perfiles.save(any())).thenReturn(e);
        assertEquals("12345", a.guardar(d).cedula());
        when(perfiles.findById(UUID.fromString(ID))).thenReturn(Optional.of(e));
        assertEquals("Ana", a.buscarPorUsuarioId(ID).nombres());
        assertNull(a.buscarPorUsuarioId("22222222-2222-2222-2222-222222222222"));
        when(perfiles.findByCedula("12345")).thenReturn(Optional.of(e));
        assertEquals(ID, a.buscarPorCedula("12345").usuarioId());
        assertNull(a.buscarPorCedula("0"));
    }

    @Test
    void tokensRevocados() {
        SpringDataTokenRevocadoRepository tokens = mock(SpringDataTokenRevocadoRepository.class);
        TokenRevocadoJpaAdapter a = new TokenRevocadoJpaAdapter(tokens);
        a.guardar(new TokenRevocado("jti-1", 9000));
        ArgumentCaptor<TokenRevocadoEntity> e = ArgumentCaptor.forClass(TokenRevocadoEntity.class);
        verify(tokens).save(e.capture());
        assertEquals("jti-1", e.getValue().getJti());
        when(tokens.existsById("jti-1")).thenReturn(true);
        assertTrue(a.existePorJti("jti-1"));
        when(tokens.eliminarVencidosAntesDe(any())).thenReturn(4);
        assertEquals(4, a.eliminarVencidosAntesDe(9000));
    }
}

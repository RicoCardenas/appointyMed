package com.appointy.auth.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.appointy.auth.domain.gateway.EmisorTokenGateway;
import com.appointy.auth.domain.gateway.TokenRevocadoRepositoryGateway;
import com.appointy.auth.domain.model.ClaimsToken;
import com.appointy.auth.domain.model.Rol;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

    final EmisorTokenGateway emisor = mock(EmisorTokenGateway.class);
    final TokenRevocadoRepositoryGateway revocados = mock(TokenRevocadoRepositoryGateway.class);
    final JwtAuthenticationFilter filtro = new JwtAuthenticationFilter(emisor, revocados);

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    void ejecutar(String cabecera) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        if (cabecera != null) req.addHeader("Authorization", cabecera);
        filtro.doFilter(req, new MockHttpServletResponse(), new MockFilterChain());
    }

    @Test
    void autenticaConUnTokenValidoYNoRevocado() throws Exception {
        when(emisor.validar("tok")).thenReturn(new ClaimsToken("u1", Rol.ADMINISTRATIVO, "jti-1", 9000));
        ejecutar("Bearer tok");
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("ROLE_ADMINISTRATIVO", auth.getAuthorities().iterator().next().getAuthority());
        UsuarioAutenticado u = (UsuarioAutenticado) auth.getPrincipal();
        assertEquals("jti-1", u.jti());
        assertEquals(9000, u.expiraEn());
    }

    @Test
    void noAutenticaSinTokenInvalidoORevocado() throws Exception {
        ejecutar(null);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        ejecutar("Basic abc");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        ejecutar("Bearer malo");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        when(emisor.validar("revocado")).thenReturn(new ClaimsToken("u1", Rol.PACIENTE, "jti-2", 9000));
        when(revocados.existePorJti("jti-2")).thenReturn(true);
        ejecutar("Bearer revocado");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

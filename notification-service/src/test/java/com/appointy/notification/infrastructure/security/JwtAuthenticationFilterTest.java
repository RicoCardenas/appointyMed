package com.appointy.notification.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.appointy.notification.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

    static final String SECRETO = "secreto-de-prueba-con-mas-de-32-caracteres-ok";
    final ValidadorJwt validador = new ValidadorJwt(new JwtProperties(SECRETO, "appointy-auth"));
    final JwtAuthenticationFilter filtro = new JwtAuthenticationFilter(validador);

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    String token(String rol) {
        return Jwts.builder().subject(UUID.randomUUID().toString()).issuer("appointy-auth").claim("rol", rol)
                .expiration(new Date(System.currentTimeMillis() + 60_000)).signWith(Keys.hmacShaKeyFor(SECRETO.getBytes(StandardCharsets.UTF_8))).compact();
    }

    MockFilterChain ejecutar(String cabecera) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        if (cabecera != null) req.addHeader("Authorization", cabecera);
        MockFilterChain cadena = new MockFilterChain();
        filtro.doFilter(req, new MockHttpServletResponse(), cadena);
        return cadena;
    }

    @Test
    void unTokenValidoAutenticaConSuRol() throws Exception {
        MockFilterChain cadena = ejecutar("Bearer " + token("PROFESIONAL"));
        assertNotNull(cadena.getRequest());
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("ROLE_PROFESIONAL", auth.getAuthorities().iterator().next().getAuthority());
        assertEquals("PROFESIONAL", ((UsuarioAutenticado) auth.getPrincipal()).rol());
    }

    @Test
    void sinCabeceraConEsquemaDistintoOTokenInvalidoNoAutentica() throws Exception {
        ejecutar(null);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        ejecutar("Basic abc");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        ejecutar("Bearer token.basura.invalido");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

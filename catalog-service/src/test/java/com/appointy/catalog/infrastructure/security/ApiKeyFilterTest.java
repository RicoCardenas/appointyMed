package com.appointy.catalog.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class ApiKeyFilterTest {

    static final String LLAVE = "llave-de-prueba-de-24-caracteres-o-mas";
    final ApiKeyFilter filtro = new ApiKeyFilter(LLAVE);

    @BeforeEach
    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private void ejecutar(String llave) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/internal/parametros");
        if (llave != null) {
            req.addHeader("X-Api-Key", llave);
        }
        MockFilterChain cadena = new MockFilterChain();
        filtro.doFilter(req, new MockHttpServletResponse(), cadena);
        assertNotNull(cadena.getRequest(), "la cadena debe continuar; la decision de acceso la toma Spring Security");
    }

    @Test
    void conLaLlaveCorrectaAutenticaComoServicio() throws Exception {
        ejecutar(LLAVE);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SERVICIO")));
    }

    @Test
    void sinLlaveOConUnaIncorrectaNoAutentica() throws Exception {
        ejecutar(null);
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        ejecutar(LLAVE + "x");
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        ejecutar("");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("X-Api-Key", ApiKeyFilter.CABECERA);
    }
}

package com.appointy.catalog.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** RNF-04: /internal/** solo acepta la llave de servicio, comparada en tiempo constante. No es un bean. */
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String CABECERA = "X-Api-Key";

    private final byte[] llave;

    public ApiKeyFilter(String llave) {
        this.llave = llave.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String recibida = request.getHeader(CABECERA);
        if (recibida != null && MessageDigest.isEqual(llave, recibida.getBytes(StandardCharsets.UTF_8))) {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    "servicio", null, List.of(new SimpleGrantedAuthority("ROLE_SERVICIO"))));
        }
        chain.doFilter(request, response);
    }
}

package com.appointy.auth.infrastructure.security;

import com.appointy.auth.domain.model.ClaimsToken;
import com.appointy.auth.domain.port.out.EmisorTokenPort;
import com.appointy.auth.domain.port.out.TokenRevocadoRepositoryPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Valida el Bearer localmente y descarta los tokens revocados por cierre de sesion. No es un bean: lo crea SecurityConfig. */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final EmisorTokenPort emisor;
    private final TokenRevocadoRepositoryPort revocados;

    public JwtAuthenticationFilter(EmisorTokenPort emisor, TokenRevocadoRepositoryPort revocados) {
        this.emisor = emisor;
        this.revocados = revocados;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            Optional<ClaimsToken> claims = emisor.validar(cabecera.substring(PREFIJO.length()).trim());
            if (claims.isPresent() && !revocados.existePorJti(claims.get().jti())) {
                ClaimsToken c = claims.get();
                var principal = new UsuarioAutenticado(c.usuarioId(), c.rol(), c.jti(), c.expiraEn());
                var autenticacion = new UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + c.rol().name())));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            }
        }
        chain.doFilter(request, response);
    }
}

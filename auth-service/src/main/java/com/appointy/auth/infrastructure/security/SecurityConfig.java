package com.appointy.auth.infrastructure.security;

import com.appointy.auth.domain.gateway.EmisorTokenGateway;
import com.appointy.auth.domain.gateway.TokenRevocadoRepositoryGateway;
import com.appointy.auth.infrastructure.config.InternalProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final int MINIMO_CARACTERES_LLAVE = 24;

    /** Cadena 1: servicio a servicio. Solo la llave abre /internal/**; un JWT de usuario no sirve aqui (RNF-04). */
    @Bean
    @Order(1)
    SecurityFilterChain cadenaInterna(HttpSecurity http, InternalProperties interna, JsonErrorWriter errores) throws Exception {
        if (interna.apiKey() == null || interna.apiKey().length() < MINIMO_CARACTERES_LLAVE) {
            throw new IllegalStateException("INTERNAL_API_KEY debe tener al menos " + MINIMO_CARACTERES_LLAVE + " caracteres");
        }
        http.securityMatcher("/internal/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a.anyRequest().hasRole("SERVICIO"))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                errores.escribir(res, HttpServletResponse.SC_UNAUTHORIZED, "NO_AUTENTICADO", "Se requiere la llave de servicio"))
                        .accessDeniedHandler((req, res, ex) ->
                                errores.escribir(res, HttpServletResponse.SC_FORBIDDEN, "ACCESO_DENEGADO", "No tienes permiso para esta operacion")))
                .addFilterBefore(new ApiKeyFilter(interna.apiKey()), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Cadena 2: usuarios finales con el JWT de auth. */
    @Bean
    @Order(2)
    SecurityFilterChain cadenaDeSeguridad(
            HttpSecurity http, EmisorTokenGateway emisor, TokenRevocadoRepositoryGateway revocados, JsonErrorWriter errores)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login")
                        .permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                errores.escribir(res, HttpServletResponse.SC_UNAUTHORIZED, "NO_AUTENTICADO", "Se requiere un token valido"))
                        .accessDeniedHandler((req, res, ex) ->
                                errores.escribir(res, HttpServletResponse.SC_FORBIDDEN, "ACCESO_DENEGADO", "No tienes permiso para esta operacion")))
                .addFilterBefore(new JwtAuthenticationFilter(emisor, revocados), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

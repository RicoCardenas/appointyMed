package com.appointy.auth.infrastructure.security;

import com.appointy.auth.domain.port.out.EmisorTokenPort;
import com.appointy.auth.domain.port.out.TokenRevocadoRepositoryPort;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain cadenaDeSeguridad(
            HttpSecurity http, EmisorTokenPort emisor, TokenRevocadoRepositoryPort revocados, JsonErrorWriter errores)
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

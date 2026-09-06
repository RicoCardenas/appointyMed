package com.appointymed.auth.infrastructure.security;

import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.usecase.JwtTokenProvider;
import com.appointymed.auth.infrastructure.persistence.repository.UserRepositoryImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepositoryImpl userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepositoryImpl userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            DomainId userId = jwtTokenProvider.getUserIdFromAccessToken(token);
            var domainRoles = jwtTokenProvider.getRolesFromAccessToken(token);
            List<String> roles = new java.util.ArrayList<>();
            for (int index = 0; index < domainRoles.size(); index++) roles.add(domainRoles.get(index));

            User user = userRepository.findById(userId).orElse(null);
            if (user == null || user.getStatus() != com.appointymed.auth.domain.model.UserStatus.ACTIVE) {
                filterChain.doFilter(request, response);
                return;
            }

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(UUID.fromString(userId.value()), null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            // Invalid token - continue without authentication
        }

        filterChain.doFilter(request, response);
    }
}

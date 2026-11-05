package com.appointy.catalog.infrastructure.security;

import com.appointy.catalog.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/** Valida localmente (RNF-03) los tokens que emite auth-service; no consulta a auth. */
@Component
public class ValidadorJwt {

    private static final int MINIMO_BYTES_SECRETO = 32;
    private static final Set<String> ROLES = Set.of("PACIENTE", "PROFESIONAL", "ADMINISTRATIVO");

    private final JwtProperties propiedades;
    private final SecretKey clave;

    public ValidadorJwt(JwtProperties propiedades) {
        byte[] secreto = propiedades.secret() == null ? new byte[0] : propiedades.secret().getBytes(StandardCharsets.UTF_8);
        if (secreto.length < MINIMO_BYTES_SECRETO) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos " + MINIMO_BYTES_SECRETO + " caracteres");
        }
        this.propiedades = propiedades;
        this.clave = Keys.hmacShaKeyFor(secreto);
    }

    /** Devuelve el usuario del token, o null si la firma, el emisor, la vigencia o el rol no son validos. */
    public UsuarioAutenticado validar(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).requireIssuer(propiedades.issuer()).build().parseSignedClaims(token).getPayload();
            String sujeto = claims.getSubject();
            UUID.fromString(sujeto);
            String rol = claims.get("rol", String.class);
            return ROLES.contains(rol) ? new UsuarioAutenticado(sujeto, rol) : null;
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}

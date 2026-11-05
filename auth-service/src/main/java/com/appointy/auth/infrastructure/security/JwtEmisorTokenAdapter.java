package com.appointy.auth.infrastructure.security;

import com.appointy.auth.domain.model.ClaimsToken;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.TokenEmitido;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.port.out.EmisorTokenPort;
import com.appointy.auth.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/** JWT firmado con HMAC; los demas servicios lo validan localmente con el mismo secreto (RNF-03). */
@Component
public class JwtEmisorTokenAdapter implements EmisorTokenPort {

    private static final int MINIMO_BYTES_SECRETO = 32;
    private static final String CLAIM_ROL = "rol";

    private final JwtProperties propiedades;
    private final Clock reloj;
    private final SecretKey clave;

    public JwtEmisorTokenAdapter(JwtProperties propiedades, Clock reloj) {
        byte[] secreto = propiedades.secret() == null ? new byte[0] : propiedades.secret().getBytes(StandardCharsets.UTF_8);
        if (secreto.length < MINIMO_BYTES_SECRETO) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos " + MINIMO_BYTES_SECRETO + " caracteres");
        }
        this.propiedades = propiedades;
        this.reloj = reloj;
        this.clave = Keys.hmacShaKeyFor(secreto);
    }

    @Override
    public TokenEmitido emitir(Usuario usuario) {
        Instant ahora = reloj.instant();
        Instant expira = ahora.plus(propiedades.expirationMinutes(), ChronoUnit.MINUTES);
        String jti = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .id(jti)
                .issuer(propiedades.issuer())
                .subject(usuario.id())
                .claim(CLAIM_ROL, usuario.rol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expira))
                .signWith(clave)
                .compact();
        return new TokenEmitido(token, jti, expira.toEpochMilli());
    }

    @Override
    public ClaimsToken validar(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(clave)
                    .requireIssuer(propiedades.issuer())
                    .clock(() -> Date.from(reloj.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String sujeto = claims.getSubject();
            UUID.fromString(sujeto); // el sujeto debe ser un UUID valido
            return new ClaimsToken(
                    sujeto,
                    Rol.valueOf(claims.get(CLAIM_ROL, String.class)),
                    claims.getId(),
                    claims.getExpiration().getTime());
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}

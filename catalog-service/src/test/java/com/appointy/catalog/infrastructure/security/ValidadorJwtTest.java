package com.appointy.catalog.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.appointy.catalog.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ValidadorJwtTest {

    static final String SECRETO = "secreto-de-prueba-de-al-menos-32-caracteres";
    static final String EMISOR = "appointy-auth";
    final ValidadorJwt validador = new ValidadorJwt(new JwtProperties(SECRETO, EMISOR));
    final String usuarioId = UUID.randomUUID().toString();

    private static String token(String secreto, String emisor, String sujeto, String rol, Instant expira) {
        return Jwts.builder()
                .subject(sujeto)
                .issuer(emisor)
                .claim("rol", rol)
                .expiration(Date.from(expira))
                .signWith(Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    @Test
    void aceptaUnTokenVigenteDeAuth() {
        UsuarioAutenticado u = validador.validar(token(SECRETO, EMISOR, usuarioId, "ADMINISTRATIVO", Instant.now().plusSeconds(600)));

        assertNotNull(u);
        assertEquals(usuarioId, u.usuarioId());
        assertEquals("ADMINISTRATIVO", u.rol());
    }

    @Test
    void rechazaTokenVencidoFirmaAjenaEmisorAjenoRolDesconocidoSujetoInvalidoOBasura() {
        Instant vigente = Instant.now().plusSeconds(600);
        assertNull(validador.validar(token(SECRETO, EMISOR, usuarioId, "PACIENTE", Instant.now().minusSeconds(60))));
        assertNull(validador.validar(token("otro-secreto-distinto-de-32-caracteres-o-mas", EMISOR, usuarioId, "PACIENTE", vigente)));
        assertNull(validador.validar(token(SECRETO, "otro-emisor", usuarioId, "PACIENTE", vigente)));
        assertNull(validador.validar(token(SECRETO, EMISOR, usuarioId, "SUPERUSUARIO", vigente)));
        assertNull(validador.validar(token(SECRETO, EMISOR, "no-es-uuid", "PACIENTE", vigente)));
        assertNull(validador.validar("basura"));
        assertNull(validador.validar(""));
    }

    @Test
    void noArrancaConUnSecretoCorto() {
        assertThrows(IllegalStateException.class, () -> new ValidadorJwt(new JwtProperties("corto", EMISOR)));
        assertThrows(IllegalStateException.class, () -> new ValidadorJwt(new JwtProperties(null, EMISOR)));
    }
}

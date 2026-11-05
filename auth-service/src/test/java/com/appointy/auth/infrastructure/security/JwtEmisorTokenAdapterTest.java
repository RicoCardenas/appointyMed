package com.appointy.auth.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.appointy.auth.domain.model.ClaimsToken;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.TokenEmitido;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.infrastructure.config.JwtProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtEmisorTokenAdapterTest {

    static final String SECRETO = "secreto-de-prueba-de-al-menos-32-caracteres";
    static final Instant T0 = Instant.parse("2026-06-01T10:00:00Z");

    final Usuario usuario = new Usuario(UUID.randomUUID().toString(), "ana@correo.com", "hash", Rol.PROFESIONAL, true, T0.toEpochMilli(), T0.toEpochMilli(), 0L);

    private static JwtEmisorTokenAdapter adaptador(String secreto, String emisor, Instant ahora) {
        return new JwtEmisorTokenAdapter(new JwtProperties(secreto, 15, emisor), Clock.fixed(ahora, ZoneOffset.UTC));
    }

    @Test
    void elTokenEmitidoSeValidaConSusDatos() {
        TokenEmitido emitido = adaptador(SECRETO, "appointy-auth", T0).emitir(usuario);

        ClaimsToken claims = adaptador(SECRETO, "appointy-auth", T0.plusSeconds(60)).validar(emitido.token());

        assertNotNull(claims);
        assertEquals(usuario.id(), claims.usuarioId());
        assertEquals(Rol.PROFESIONAL, claims.rol());
        assertEquals(emitido.jti(), claims.jti());
        assertEquals(T0.plusSeconds(900).toEpochMilli(), emitido.expiraEn());
        assertEquals(emitido.expiraEn(), claims.expiraEn());
    }

    @Test
    void cadaTokenLlevaUnJtiDistinto() {
        JwtEmisorTokenAdapter a = adaptador(SECRETO, "appointy-auth", T0);
        assertNotEquals(a.emitir(usuario).jti(), a.emitir(usuario).jti());
    }

    @Test
    void rechazaUnTokenVencido() {
        String token = adaptador(SECRETO, "appointy-auth", T0).emitir(usuario).token();

        assertNull(adaptador(SECRETO, "appointy-auth", T0.plusSeconds(15 * 60 + 1)).validar(token));
    }

    @Test
    void rechazaUnTokenFirmadoConOtroSecreto() {
        String token = adaptador("otro-secreto-distinto-de-32-caracteres-o-mas", "appointy-auth", T0).emitir(usuario).token();

        assertNull(adaptador(SECRETO, "appointy-auth", T0).validar(token));
    }

    @Test
    void rechazaUnTokenDeOtroEmisor() {
        String token = adaptador(SECRETO, "otro-emisor", T0).emitir(usuario).token();

        assertNull(adaptador(SECRETO, "appointy-auth", T0).validar(token));
    }

    @Test
    void rechazaTextoQueNoEsUnToken() {
        JwtEmisorTokenAdapter a = adaptador(SECRETO, "appointy-auth", T0);
        assertNull(a.validar("basura"));
        assertNull(a.validar(""));
    }

    @Test
    void noArrancaConUnSecretoCorto() {
        assertThrows(IllegalStateException.class, () -> adaptador("corto", "appointy-auth", T0));
        assertThrows(IllegalStateException.class, () -> adaptador(null, "appointy-auth", T0));
    }
}

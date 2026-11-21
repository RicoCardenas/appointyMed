package com.appointy.auth.domain.usecase;

import com.appointy.auth.domain.exception.CredencialesInvalidasException;
import com.appointy.auth.domain.gateway.CodificadorClaveGateway;
import com.appointy.auth.domain.gateway.EmisorTokenGateway;
import com.appointy.auth.domain.gateway.RelojGateway;
import com.appointy.auth.domain.gateway.TokenRevocadoRepositoryGateway;
import com.appointy.auth.domain.gateway.TransactionGateway;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.TokenEmitido;
import com.appointy.auth.domain.model.TokenRevocado;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.rule.NormalizadorCorreo;

/** Sesion: iniciar (CU-02), cerrar (CU-03) y limpiar tokens revocados vencidos (RF-03). */
public class SesionUseCase {

    public record IniciarSesionComando(String correo, String clave) {}

    /** {@code expiraEn}: milisegundos desde 1970 en UTC. */
    public record IniciarSesionResultado(String token, long expiraEn, String usuarioId, Rol rol) {}

    /** {@code expiraEn}: milisegundos desde 1970 en UTC. */
    public record CerrarSesionComando(String jti, long expiraEn) {}

    private final UsuarioRepositoryGateway usuarios;
    private final CodificadorClaveGateway codificador;
    private final EmisorTokenGateway emisor;
    private final TokenRevocadoRepositoryGateway revocados;
    private final RelojGateway reloj;
    private final TransactionGateway transacciones;
    /** Se compara contra este hash cuando el correo no existe, para que el tiempo no delate si la cuenta existe. */
    private final String hashSenuelo;

    public SesionUseCase(UsuarioRepositoryGateway usuarios, CodificadorClaveGateway codificador, EmisorTokenGateway emisor,
            TokenRevocadoRepositoryGateway revocados, RelojGateway reloj, TransactionGateway transacciones) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.emisor = emisor;
        this.revocados = revocados;
        this.reloj = reloj;
        this.transacciones = transacciones;
        this.hashSenuelo = codificador.codificar("senuelo-sin-uso");
    }

    public IniciarSesionResultado iniciarSesion(IniciarSesionComando comando) {
        return transacciones.ejecutarSoloLectura(() -> {
            Usuario usuario = usuarios.buscarPorCorreo(NormalizadorCorreo.comparable(comando.correo()));
            String hash = usuario != null ? usuario.claveHash() : hashSenuelo;
            boolean claveCorrecta = comando.clave() != null && codificador.coincide(comando.clave(), hash);
            if (usuario == null || !claveCorrecta || !usuario.activo()) {
                throw new CredencialesInvalidasException();
            }
            TokenEmitido token = emisor.emitir(usuario);
            return new IniciarSesionResultado(token.token(), token.expiraEn(), usuario.id(), usuario.rol());
        });
    }

    /** Revoca el token actual hasta su vencimiento natural. */
    public void cerrarSesion(CerrarSesionComando comando) {
        transacciones.ejecutar(() -> {
            if (!revocados.existePorJti(comando.jti())) {
                revocados.guardar(new TokenRevocado(comando.jti(), comando.expiraEn()));
            }
            return null;
        });
    }

    /** Elimina de la lista de revocados los tokens que ya expiraron por si solos. Devuelve cuantos. */
    public int limpiarTokensVencidos() {
        return transacciones.ejecutar(() -> revocados.eliminarVencidosAntesDe(reloj.ahora()));
    }
}
